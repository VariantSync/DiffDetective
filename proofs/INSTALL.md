## Installation Instructions

As explained in the [REQUIREMENTS.md](REQUIREMENTS.md), the [_Stack_][stack] build system is our only installation requirement.

### Setup
First, please install Stack.
Detailed installation instructions for many operating systems are given on the respective [installation webpage][stackinstall].

- Linux: You can install stack via `curl -sSL https://get.haskellstack.org/ | sh` or `wget -qO- https://get.haskellstack.org/ | sh` (Alternatively, if you are using an Ubuntu-based distro, you can get it with apt `sudo apt-get install haskell-stack`, or `sudo pacman -S stack` if you are using an Arch-based distro).
  Further instructions for installing stack including specific linux distributions are given [here][stackinstall].
- Windows 64-bit: Go to the [stack installation page][stackinstall]. Download and run the _Windows 64-bit Installer_.
- MacOS: Please follow the instructions on the [installation webpage][stackinstall].

Second, please open a terminal and navigate to the proofs directory of this repository (the directory containing this `INSTALL.md`).
```shell
cd <path/to/this/repository/proofs>
```
Before running the example you should update and upgrade stack as you might get errors otherwise:
```shell
stack update
stack upgrade
```
You can then build the library and run the example as follows:
```shell
stack run
```

## What Is There to See

Our example executes a simple test. It 
1. constructs two variation trees (called `Kanto Starters` and `Johto Starters`) and prints them to terminal;
2. diffs both trees to a variation tree diff using our [`naiveDiff`](src/VariationDiff.hs) function, which is described in our appendix, and prints the diff to terminal;
3. creates both projections of the variation tree diff (before and after the edit) and prints both to terminal;
4. and finally asserts that both projections equal the initial variation trees.

The expected output is:

```text
>>>>>>> Kanto Starters >>>>>>>
Variation Tree with edges {
  (Artifact "Bulbasaur", 1) -> (Mapping "Grass", 4)
  (Artifact "Charmander", 2) -> (Mapping "Fire", 5)
  (Artifact "Squirtle", 3) -> (Mapping "Water", 6)
  (Mapping "Grass", 4) -> (Mapping ⊤, 0)
  (Mapping "Fire", 5) -> (Mapping ⊤, 0)
  (Mapping "Water", 6) -> (Mapping ⊤, 0)
}

>>>>>>> Johto Starters >>>>>>>
Variation Tree with edges {
  (Artifact "Chikorita", 7) -> (Mapping "Grass", 10)
  (Artifact "Cyndaquil", 8) -> (Mapping "Fire", 11)
  (Artifact "Totodile", 9) -> (Mapping "Water", 12)
  (Mapping "Grass", 10) -> (Mapping ⊤, 0)
  (Mapping "Fire", 11) -> (Mapping ⊤, 0)
  (Mapping "Water", 12) -> (Mapping ⊤, 0)
}

>>>>>>> Naive Diff >>>>>>>
Variation Diff with edges {
  (REM, Artifact "Bulbasaur", 1) -REM-> (REM, Mapping "Grass", 4)
  (REM, Artifact "Charmander", 2) -REM-> (REM, Mapping "Fire", 5)
  (REM, Artifact "Squirtle", 3) -REM-> (REM, Mapping "Water", 6)
  (REM, Mapping "Grass", 4) -REM-> (NON, Mapping ⊤, 0)
  (REM, Mapping "Fire", 5) -REM-> (NON, Mapping ⊤, 0)
  (REM, Mapping "Water", 6) -REM-> (NON, Mapping ⊤, 0)
  (ADD, Artifact "Chikorita", 7) -ADD-> (ADD, Mapping "Grass", 10)
  (ADD, Artifact "Cyndaquil", 8) -ADD-> (ADD, Mapping "Fire", 11)
  (ADD, Artifact "Totodile", 9) -ADD-> (ADD, Mapping "Water", 12)
  (ADD, Mapping "Grass", 10) -ADD-> (NON, Mapping ⊤, 0)
  (ADD, Mapping "Fire", 11) -ADD-> (NON, Mapping ⊤, 0)
  (ADD, Mapping "Water", 12) -ADD-> (NON, Mapping ⊤, 0)
}

>>>>>>> Projected Kanto >>>>>>>
Variation Tree with edges {
  (Artifact "Bulbasaur", 1) -> (Mapping "Grass", 4)
  (Artifact "Charmander", 2) -> (Mapping "Fire", 5)
  (Artifact "Squirtle", 3) -> (Mapping "Water", 6)
  (Mapping "Grass", 4) -> (Mapping ⊤, 0)
  (Mapping "Fire", 5) -> (Mapping ⊤, 0)
  (Mapping "Water", 6) -> (Mapping ⊤, 0)
}

>>>>>>> Projected Johto >>>>>>>
Variation Tree with edges {
  (Artifact "Chikorita", 7) -> (Mapping "Grass", 10)
  (Artifact "Cyndaquil", 8) -> (Mapping "Fire", 11)
  (Artifact "Totodile", 9) -> (Mapping "Water", 12)
  (Mapping "Grass", 10) -> (Mapping ⊤, 0)
  (Mapping "Fire", 11) -> (Mapping ⊤, 0)
  (Mapping "Water", 12) -> (Mapping ⊤, 0)
}

>>>>>>> Assert(Kanto == Projected Kanto) >>>>>>>
 ===> Elements equal! Great Success!

>>>>>>> Assert(Johto == Projected Johto) >>>>>>>
 ===> Elements equal! Great Success!
```

[stack]: https://docs.haskellstack.org/en/stable/README/
[stackinstall]: https://docs.haskellstack.org/en/stable/install_and_upgrade/
