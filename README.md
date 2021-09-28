# DiffDetective

[![Thesis](https://img.shields.io/badge/Thesis-Read-blue)][thesis]

This is the tool accompanying the bachelor's thesis [**Empirical Evaluation of Feature Trace Recording on the Edit History of Marlin**][thesis] by Sören Viegener.
(The version of DiffDetective described in and submitted with the thesis can be found on branch `thesis-sv`).

DiffDetective is a library to analyse the evolution of variability in source code in preprocessor-based software product lines.
It serves two main purposes:
1. DiffDetective parses diffs on preprocessor annotated source code to so called `DiffTrees`. For example, the following diff in which
   the annotation `DEBUG` gets removed and an `else` case is added
    ```diff
      #if A
        x = 0;
    -   #if DEBUG
          print(x);
    -   #endif
    + #else
    +   x = 1;
      #endif
    ```
   can be parsed to the following graph structure to analyse the diff:

    ![difftreeshowcase](docs/showcase/examplediff.png)

3. DiffDetective takes a preprocessor-based software product line repository as input and matches edit patterns in its commit history.
It can detect an extensible variety of different edit patterns and reverse engineers feature contexts known from feature trace recording.
The output of the tool consists of all pattern matches found and different metrics relevant for the evaluation of feature trace recording.

## Related Work

**Feature Trace Recording**.
Paul Maximilian Bittner, Alexander Schultheiß, Thomas Thüm, Timo Kehrer, Jeffrey Young, and Lukas Linsbauer.
*ESEC/FSE'21. ACM, New York, NY, USA. August 2021*:
https://pmbittner.github.io/FeatureTraceRecording/

### Edit Patterns
**Concepts, Operations, and Feasibility of a Projection-Based Variation Control System**.
Stefan Stănciulescu, Thorsten Berger, Eric Walkingshaw, and Andrzej Wąsowski.
https://ieeexplore.ieee.org/document/7816478

[thesis]: https://oparu.uni-ulm.de/xmlui/handle/123456789/38679