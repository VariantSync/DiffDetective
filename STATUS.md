The artifact for the paper _Classifying Edits to Variability in Source Code_ consists of four parts:

1. **Appendix**: The appendix of our paper is given in PDF format in the file [`appendix.pdf`][ddappendix].
2. **DiffDetective**: For our validation, we built DiffDetective, a java library and command-line tool to classify edits to variability in git histories of preprocessor-based software product lines. DiffDetective is the main artifact used to replicate the validation for our paper (see Section 5).
3. **Haskell Formalization**: We provide an extended formalization in the Haskell programming language as described in our appendix. Its implementation can be found in the Haskell project in the [`proofs`][ddproofs] directory.
4. **Dataset Overview**: We provide an overview of the 44 inspected open-source software product lines with updated links to their repositories in the file [docs/datasets.md](docs/datasets.md).

Our artifact has the following purposes:

- **Replicability**: We provide instructions based on a docker container that allow to reproduce the validation we performed in Section 5 of our paper.
- **Reusability**: DiffDetective is designed as a library that offers reusable functionality.
Researchers and practitioners can use our DiffDetective library to build on our theory and results (e.g., for future prototypes to study the evolution of variability in source code).
DiffDetective offers various features, including but not limited to parsing variation tree diffs from unix diffs, obtaining variation tree diffs for certain patches and commits, matching elementary edit patterns on variation tree diffs, defining custom classifications, rendering, traversing, and transformations of variation tree diffs, various de-/serialization methods, and running analyses for the git histories of C preprocessor-based software product lines. We documented each part of the library and provide a javadoc website within the repository.
- **Extended Formal Specification**: The `proofs` Haskell project provides an extended formal specification of our theory.
Its main purpose is to document our theory and its extensions and to serve as a reference for the proofs in our appendix.
The project can also be used as a library to reason on variation trees and diffs in Haskell projects.
The library is accompanied with a small demo application that shows an example test case for our proof for completeness, by creating a variation tree diff from two variation trees and re-projecting them.

We claim the **_Artifacts Available_** badge as we made our artefacts publicly available on [Github][ddgithub] and [Zenodo][ddzenodo]. (For now, the Zenodo link points to a reserved doi under which we will publish the artifacts once completed until the deadline at June 4.)

We claim the **_Artifacts Evaluated Reusable_** badge as we implemented DiffDetective as a reusable library (see above).
Furthermore, both DiffDetective and our Haskell formalization serve as reference implementations if researchers or practitioners want to reimplement our theory in another programming language.

[ddgithub]: UNDEFINED
[ddzenodo]: UNDEFINED
[ddappendix]: UNDEFINED
[ddproofs]: UNDEFINED
