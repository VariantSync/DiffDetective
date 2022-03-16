# Classifying Edits to Variability in Source Code

This is the replication package our submission _Classifying Edits to Variability in Source Code_ submitted to the 30th ACM Joint European Software Engineering Conference and Symposium on the Foundations of Software Engineering (ESEC/FSE) in March 2022.

This replication package consists of four parts:

1. **Appendix**: The appendix of our paper is given in PDF format in the file [appendix.pdf](appendix.pdf).
2. **DiffDetective**: For our validation, we built DiffDetective, a java library and command-line tool to classify edits to variability in git histories of preprocessor-based software product lines.
3. **Haskell Formalization**: We provide an extended formalization in the Haskell programming language as described in our appendix. Its implementation can be found in the Haskell project in the [proofs](proofs) directory.
4. **Dataset Overview**: We provide an overview of the 44 inspected datasets with updated links to their repositories in the file [docs/datasets.md](docs/datasets.md).

## Appendix

Our appendix consists of:
1. An extended formalization of our concepts in the [Haskell][haskell] programming language. The corresponding source code is also part of this replication package (see below).
2. A proof for completeness of variation tree diffs to represent edits to variation trees.
3. An inspection of edit patterns from related work to show that existing patterns are either composite patterns built from our elementary patterns or similar to our elementary patterns.
4. The complete results of our validation for all 44 datasets.

## DiffDetective

**DESCRIBE HOW TO RUN EXPERIMENT HERE**

## Haskell Formalization

**@Alex: Bitte die Beschreibung für das Docker Setup in der [proofs/INSTALL.md](proofs/INSTALL.md) anpassen!**

The extended formalization in Haskell is a library using the _Stack_ build system.
Instructions for installing Stack are given in [proofs/REQUIREMENTS.md](proofs/REQUIREMENTS.md).
How to build our library and how to run the example is described in the [proofs/INSTALL.md](proofs/INSTALL.md).

[haskell]: https://www.haskell.org/
