# STATUS
## Overview
The artifact for the paper _Classifying Edits to Variability in Source Code_ consists of four parts:

1. **DiffDetective**: For our validation, we built DiffDetective, a java library and command-line tool to classify edits to variability in git histories of preprocessor-based software product lines. 
  DiffDetective is the main artifact used to replicate the validation of our paper (see Section 5).
  DiffDetective is self-contained in that it does not require or depend on in-depth knowledge on the theoretical foundation of our work.
  Practitioners and researches are free to ignore the appendix as well as the haskell formalization and may use DiffDetective out-of-the-box.
2. **Appendix**: The appendix of our paper is given in PDF format in the file [`appendix.pdf`][ddappendix].
3. **Haskell Formalization**: We provide an extended formalization in the Haskell programming language as described in our appendix. Its implementation can be found in the Haskell project in the [`proofs`][ddproofs] directory.
4. **Dataset Overview**: We provide an overview of the 44 inspected open-source software product lines with updated links to their repositories in the file [docs/datasets/all.md][dddatasets].

## Purpose
Our artifact has the following purposes:

### **Replicability**
We provide replication instructions that allow to replicate the validation we performed in Section 5 of our paper.
The replication is executed in a Docker container. To replicate our results, we also provide [forks of all 44 datasets][ddforks] in the very state we performed our validation on.

### **Reusability**
DiffDetective is designed as a library that offers reusable functionality.
Researchers and practitioners can use our DiffDetective library to build on our theory and results (e.g., for future prototypes to study the evolution of variability in source code).

DiffDetective offers various features, including but not limited to:
parsing variation diffs from unix diffs, obtaining variation diffs for certain patches and commits, classifying edits in variation diffs, defining custom classifications, rendering, traversing, and transforming variation diffs, various de-/serialization methods, and running analyses for the git histories of C preprocessor-based software product lines. We documented each part of the library and provide a [javadoc website][dddocumentation] within the repository.
Moreover, our validation (see _replicability_ above) may also be run on any custom dataset as described in our [README.md][ddreadme].

### **Extended Formal Specification**
The [`proofs`][ddproofs] Haskell project provides an extended formal specification of our theory.
Its main purpose is to document the theory and its extensions to serve as a reference for the proofs in our appendix.
Yet, the project can also be used as a library to reason on variation trees and diffs in Haskell projects.
The library is accompanied by a small demo application that shows an example test case for our proof of completeness by creating a variation diff from two variation trees and re-projecting them.
The `proofs` project is described in detail in our appendix.

## Claims
We claim the _Artifacts Available_ badge as we made our artifacts publicly available on [Github][ddgithub] and [Zenodo][ddzenodo] with an [open-source license][ddlicense]. All [44 input datasets][ddforks] are open-source projects and publicly available.

We claim the _Artifacts Evaluated Reusable_ badge as we implemented DiffDetective as a reusable library (see above).
Furthermore, both DiffDetective and our Haskell formalization serve as reference implementations if researchers or practitioners want to reimplement our theory in other programming languages.

[ddgithub]: https://github.com/VariantSync/DiffDetective/tree/esecfse22
[ddzenodo]: https://doi.org/10.5281/zenodo.6818140
[ddreadme]: https://github.com/VariantSync/DiffDetective/tree/esecfse22/README.md
[ddappendix]: https://github.com/VariantSync/DiffDetective/raw/esecfse22/appendix.pdf
[ddproofs]: https://github.com/VariantSync/DiffDetective/tree/esecfse22/proofs
[ddlicense]: https://github.com/VariantSync/DiffDetective/blob/main/LICENSE.LGPL3
[dddatasets]: ../../docs/datasets/all.md
[ddforks]: ../../docs/datasets/esecfse22-replication.md
[dddocumentation]: https://variantsync.github.io/DiffDetective/docs/javadoc/
