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
  buildJavadoc ? true,
}:
pkgs.stdenv.mkDerivation rec {
  pname = "DiffDetective";
  version = "1.0.0";
  src = ./.;

  nativeBuildInputs = with pkgs; [
    maven
    makeWrapper
    graphviz
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
    outputHash = "sha256-gmbyhqgMMZxt3+7ov/Zgm1EGdZBhn4WfAj8yphhg2CA=";
  };

  buildPhase = ''
    runHook preBuild

    mvn --offline -Dmaven.repo.local="$mavenRepo" -Dmaven.test.skip=true clean package ${
      if buildJavadoc
      then "javadoc:javadoc"
      else ""
    }

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
    makeWrapper "${pkgs.maven.jdk}/bin/java" "$out/bin/DiffDetective" --add-flags "-cp \"$jar\"" \
      --prefix PATH : "${pkgs.graphviz}"

    ${
      if buildJavadoc
      then ''
        local doc="$out/share/doc"
        mkdir -p "$doc"
        cp -r docs/javadoc "$doc/DiffDetective"
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
