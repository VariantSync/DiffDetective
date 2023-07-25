[![Documentation](https://img.shields.io/badge/Documentation-Read-purple)][documentation]
[![Install](https://img.shields.io/badge/Install-Instructions-blue)](INSTALL.md)
[![License](https://img.shields.io/badge/License-GNU%20LGPLv3-blue)](../../LICENSE.LGPL3)
[![Artifact DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.8027920.svg)](https://doi.org/10.5281/zenodo.8027920)

# Views on Edits to Variational Software

This is the replication package for our paper _Views on Edits to Variational Software_ conditionally accepted at the 27th Systems and Software Product Line Conference (SPLC 2023).

This replication package consists of two parts:

1. **Feasibility Study**: We implemented our feasibility study in _DiffDetective_, a library and command-line tool for analysis of edits to preprocessor-based software product lines.
2. **Appendix**: The appendix of our paper is given in PDF format in the file [../../appendix/appendix-splc23-views.pdf][appendix]. The appendix consists of the proof of correctness of our optimized view generation $view_{smart}$ (Theorem 5.8 in the paper).

## Implementation of Views on Edits to Variational Software in DiffDetective

We implemented the generation of views and our experiment in DiffDetective.
In this section, we outline where our extensions can be found within DiffDetective's code base.

Our implementation of views as proposed in our paper can be found in the packages [org.variantsync.diffdetective.variation.tree.view][pkg-treeview] and [org.variantsync.diffdetective.variation.diff.view][pkg-diffview] for views on variation trees (Section 3 in our paper) and diffs (Section 5), respectively.
The algorithms $view_{naive}$ and $view_{smart}$ from Section 5.2 and 5.3 in the paper are implemented in the [DiffView][cls-diffview] class.
The sub-package [org.variantsync.diffdetective.variation.tree.view.relevance][pkg-relevance] contains the implementation of the relevance predicates to support different types of views (Section 3)

The experiment for our feasibility study (Section 6) is implemented in the package [org.variantsync.diffdetective.experiments.views][pkg-feasibilityexperiment].
The experiment's entry point is the main method in the [Main][cls-feasibilitymain] class in that package.

We documented all relevant source code of our extensions with Javadoc.
The majority of DiffDetective's code base is documented, too.
You may access the javadoc website [here][documentation].

## Replication of the Feasibility Study

**Hardware and software requirements** are documented in the [REQUIREMENTS.md](REQUIREMENTS.md) file.
Please make sure that you meet the specified software requirements (there are no specific hardware requirements).

**Instructions to setup and replicate our feasibility study** can be found in the [INSTALL.md](INSTALL.md) file, including detailed descriptions of each step.
We offer a [Docker](https://www.docker.com/) setup to replicate the feasibility study performed in our paper.

**Troubleshooting advice** for frequent errors can be found at the bottom of the [INSTALL.md](INSTALL.md) file.

[appendix]: ../../appendix/appendix-splc23-views.pdf
[documentation]: https://htmlpreview.github.io/?https://github.com/VariantSync/DiffDetective/blob/splc23-views/docs/javadoc/index.html

[pkg-treeview]: https://htmlpreview.github.io/?https://raw.githubusercontent.com/VariantSync/DiffDetective/splc23-views/docs/javadoc/org/variantsync/diffdetective/variation/tree/view/package-summary.html
[pkg-diffview]: https://htmlpreview.github.io/?https://raw.githubusercontent.com/VariantSync/DiffDetective/splc23-views/docs/javadoc/org/variantsync/diffdetective/variation/diff/view/package-summary.html
[pkg-relevance]: https://htmlpreview.github.io/?https://raw.githubusercontent.com/VariantSync/DiffDetective/splc23-views/docs/javadoc/org/variantsync/diffdetective/variation/tree/view/relevance/package-summary.html
[pkg-feasibilityexperiment]: https://htmlpreview.github.io/?https://raw.githubusercontent.com/VariantSync/DiffDetective/splc23-views/docs/javadoc/org/variantsync/diffdetective/experiments/views/package-summary.html
[cls-diffview]: https://htmlpreview.github.io/?https://raw.githubusercontent.com/VariantSync/DiffDetective/splc23-views/docs/javadoc/org/variantsync/diffdetective/variation/diff/view/DiffView.html
[cls-feasibilitymain]: https://htmlpreview.github.io/?https://raw.githubusercontent.com/VariantSync/DiffDetective/splc23-views/docs/javadoc/org/variantsync/diffdetective/experiments/views/Main.html