{
  sources ? import ./nix/sources.nix,
  system ? builtins.currentSystem,
  pkgs ?
    import sources.nixpkgs {
      overlays = [];
      config = {};
      inherit system;
    },
  doCheck ? true,
  buildGitHubPages ? true,
}:
pkgs.stdenv.mkDerivation rec {
  pname = "DiffDetective";
  version = "2.1.0";
  src = with pkgs.lib.fileset;
    toSource {
      root = ./.;
      # This should be `gitTracked ./.`. However, this currently doesn't accept
      # shallow repositories as used in GitHub CI.
      fileset =
        (import (sources.nixpkgs + "/lib/fileset/internal.nix") {inherit (pkgs) lib;})._fromFetchGit
        "gitTracked"
        "argument"
        ./.
        {shallow = true;};
    };

  nativeBuildInputs = with pkgs; [
    maven
    makeWrapper
    graphviz
    (ruby.withPackages (pkgs: with pkgs; [github-pages jekyll-theme-cayman]))
  ];

  mavenRepo = pkgs.stdenv.mkDerivation {
    pname = "${pname}-mavenRepo";
    inherit version;
    src = pkgs.lib.sourceByRegex ./. ["^pom.xml$" "^local-maven-repo(/.*)?$"];

    nativeBuildInputs = with pkgs; [maven];

    buildPhase = ''
      mvn org.apache.maven.plugins:maven-dependency-plugin:3.6.0:go-offline -Dmaven.repo.local="$out"
    '';

    # keep only *.{pom,jar,sha1,nbm} and delete all ephemeral files with lastModified timestamps inside
    installPhase = ''
      find "$out" -type f \
        \( -name \*.lastUpdated -or \
           -name resolver-status.properties -or \
           -name _remote.repositories \) \
        -delete
    '';

    dontFixup = true;
    dontConfigure = true;
    outputHashAlgo = "sha256";
    outputHashMode = "recursive";
    outputHash = "sha256-Gimt6L54yyaX3BtdhQlVu1j4c4y++Mip0GzMl/IfzMc=";
  };

  buildPhase = ''
    runHook preBuild

    mvn() {
      command mvn --offline -Dmaven.repo.local="$mavenRepo" "$@"
    }

    ${
      # Build the documentation before the code because we don't want to include
      # the generated files in the GitHub Pages
      if buildGitHubPages
      then ''
        mvn javadoc:javadoc
        JEKYLL_ENV=production PAGES_REPO_NWO=VariantSync/DiffDetective JEKYLL_BUILD_REVISION= github-pages build
      ''
      else ""
    }
    mvn -Dmaven.test.skip=true clean package

    runHook postBuild
  '';

  inherit doCheck;
  checkPhase = ''
    runHook postTest

    mvn --offline -Dmaven.repo.local="$mavenRepo" test

    runHook postTest
  '';

  installPhase = ''
    runHook postInstall

    local jar="$out/share/java/DiffDetective/DiffDetective.jar"
    install -Dm644 "target/diffdetective-${version}-jar-with-dependencies.jar" "$jar"
    makeWrapper "${pkgs.jdk}/bin/java" "$out/bin/DiffDetective" --add-flags "-cp \"$jar\"" \
      --prefix PATH : "${pkgs.graphviz}"

    ${
      if buildGitHubPages
      then ''
        mkdir "$out/share/github-pages"
        cp -r _site "$out/share/github-pages/DiffDetective"
      ''
      else ""
    }

    runHook postInstall
  '';

  meta = {
    description = "DiffDetective is a library for analysing changes to software product lines";
    homepage = "https://github.com/VariantSync/DiffDetective";
    license = pkgs.lib.licenses.lgpl3;
    platforms = pkgs.maven.meta.platforms;
    maintainers = [
      {
        name = "Benjamin Moosherr";
        email = "Benjamin.Moosherr@uni-ulm.de";
        github = "ibbem";
        githubId = 61984399;
      }
    ];
  };
}
