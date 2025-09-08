{
  sources ? import ./nix/sources.nix,
  system ? builtins.currentSystem,
  pkgs ?
    import sources.nixpkgs {
      overlays = [
        (final: previous: {
          defaultGemConfig = previous.defaultGemConfig // {
            jekyll-github-metadata = attrs: {
              dontBuild = false;
              patches = [
                (final.fetchpatch {
                  url = "https://github.com/jekyll/github-metadata/commit/17cc5af5e1fd95d98d43676610cc8a47969350ab.patch";
                  hash = "sha256-dUqvnYsjfG5xQIYS48B3xz0GLVYo2BrDAnYUafmDFKw=";
                  relative = "lib";
                  stripLen = 1;
                  extraPrefix = "lib/jekyll-github-metadata/";
                })
              ];
            };
          };
        })
      ];
      config = {};
      inherit system;
    },
  doCheck ? true,
  buildGitHubPages ? true,
  dependenciesHash ? "sha256-xQG7IjBROSXfMIe7kvU8fXfKShdqKwVaJR0y97jsZWU=",
}:
pkgs.stdenvNoCC.mkDerivation rec {
  pname = "DiffDetective";
  # The single source of truth for the version number is stored in `pom.xml`.
  # Hence, this XML file needs to be parsed to extract the current version.
  version = pkgs.lib.removeSuffix "\n" (pkgs.lib.readFile
    (pkgs.runCommandLocal "DiffDetective-version" {
      nativeBuildInputs = [pkgs.xq-xml];
    } "xq -x '/project/version' ${./pom.xml} > $out"));
  src = with pkgs.lib.fileset;
    toSource {
      root = ./.;
      fileset = gitTracked ./.;
    };

  nativeBuildInputs = [
    pkgs.maven
    pkgs.makeWrapper
  ] ++ pkgs.lib.optional buildGitHubPages (pkgs.ruby.withPackages (rubyPkgs: [
    rubyPkgs.github-pages
    rubyPkgs.jekyll-theme-cayman
  ]));

  nativeCheckInputs = [
    pkgs.graphviz
  ];

  # Maven needs to download necessary dependencies which is impure because it
  # requires network access. Hence, we download all dependencies as a
  # fixed-output derivation. This also serves as a nice cache.
  #
  # We use the hash of the input files to invalidate the maven cache whenever
  # the input files change. This is purely for easing maintenance. In case a
  # maintainer forgets (or simply doesn't know) to change the output hash to
  # force a rebuild (and obtain the new, correct hash) this usually (without
  # this naming hack) result in use of a stall cash, essentially behaving like
  # an unreproducable derivation where a second build results in a different
  # output. By changing the name of the fixed output derivation whenever
  # `mavenRepoSrc` changes, we prevent this stall cache as the resulting store
  # path will never exist (except when the cache is valid).
  mavenRepoSrc = pkgs.lib.sourceByRegex ./. ["^pom.xml$" "^local-maven-repo(/.*)?$"];
  mavenRepoSrcName = with pkgs.lib; last (splitString "/" mavenRepoSrc.outPath);
  mavenRepo = pkgs.stdenv.mkDerivation {
    pname = "${pname}-mavenRepo-${mavenRepoSrcName}";
    inherit version;
    src = mavenRepoSrc;

    nativeBuildInputs = [pkgs.maven];

    buildPhase = ''
      runHook preBuild

      mvn org.apache.maven.plugins:maven-dependency-plugin:3.6.0:go-offline -Dmaven.repo.local="$out"

      runHook postBuild
    '';

    # keep only *.{pom,jar,sha1,nbm} and delete all ephemeral files with lastModified timestamps inside
    installPhase = ''
      runHook preInstall

      find "$out" -type f \
        \( -not \( -name "*.pom" -o -name "*.jar" -o -name "*.sha1" -o -name "*.nbm" \) \
            -o -name "maven-metadata*" \) \
        -delete

      runHook postInstall
    '';

    dontFixup = true;
    dontConfigure = true;
    outputHashAlgo = "sha256";
    outputHashMode = "recursive";
    outputHash = dependenciesHash;
  };

  # - `out` contains jars, an executable wrapper and optionally documentation
  #   (see `buildGitHubPages`)
  # - `maven` contains a local maven repository with DiffDetective and all its
  #   build-time and run-time dependencies.
  outputs = ["out" "maven"];

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
        mv target/reports/apidocs docs/javadoc
        JEKYLL_ENV=production PAGES_REPO_NWO=VariantSync/DiffDetective JEKYLL_BUILD_REVISION= PAGES_DISABLE_NETWORK=1 github-pages build
        rm -rf _site/target
      ''
      else ""
    }
    mvn -Dmaven.test.skip=true clean package

    runHook postBuild
  '';

  inherit doCheck;
  checkPhase = ''
    runHook preTest

    mvn --offline -Dmaven.repo.local="$mavenRepo" test

    runHook postTest
  '';

  installPhase = ''
    runHook preInstall

    # install jars in "$out"
    install -Dm644 "target/diffdetective-$version.jar" "$out/share/java/DiffDetective.jar"
    local jar="$out/share/java/DiffDetective/DiffDetective-jar-with-dependencies.jar"
    install -Dm644 "target/diffdetective-$version-jar-with-dependencies.jar" "$jar"
    makeWrapper \
      "${pkgs.jdk}/bin/java" "$out/bin/DiffDetective" \
      --add-flags "-cp \"$jar\"" \
      --prefix PATH : "${pkgs.lib.makeBinPath [pkgs.graphviz]}"

    ${
      if buildGitHubPages
      then ''
        # install documentation in "$out"
        mkdir "$out/share/github-pages"
        cp -r _site "$out/share/github-pages/DiffDetective"
      ''
      else ""
    }

    # install DiffDetective in "$maven" by creating a copy of "$mavenRepo" as base
    cp -r "$mavenRepo" "$maven"
    chmod u+w -R "$maven"
    mvn --offline -Dmaven.repo.local="$maven" -Dmaven.test.skip=true install

    # keep only *.{pom,jar,sha1,nbm} and delete all ephemeral files with lastModified timestamps inside
    find "$maven" -type f \
      \( -not \( -name "*.pom" -o -name "*.jar" -o -name "*.sha1" -o -name "*.nbm" \) \
          -o -name "maven-metadata*" \) \
      -delete

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
