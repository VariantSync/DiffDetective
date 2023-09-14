{
  sources ? import ./sources.nix,
  system ? builtins.currentSystem,
  pkgs ?
    import sources.nixpkgs {
      overlays = [];
      config = {};
      inherit system;
    },
}: let
  DiffDetective = import ../default.nix {};
in
  pkgs.mkShell {
    inputsFrom = [DiffDetective];
    pkgs = [DiffDetective.mavenRepo];
  }
