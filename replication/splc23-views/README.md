[//]: # (![Maven]&#40;https://github.com/VariantSync/DiffDetective/actions/workflows/maven.yml/badge.svg&#41;)
[//]: # ([![Documentation]&#40;https://img.shields.io/badge/Documentation-Read-purple&#41;][documentation])
[![Install](https://img.shields.io/badge/Install-Instructions-blue)](INSTALL.md)
[![License](https://img.shields.io/badge/License-GNU%20LGPLv3-blue)](../../LICENSE.LGPL3)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7110095.svg)](https://doi.org/10.5281/zenodo.7110095)

# Views on Edits to Variational Software

This is the replication package for our paper _Views on Edits to Variational Software_ conditionally accepted at the 27th Systems and Software Product Line Conference (SPLC 2023).

This replication package consists of two parts:

1. **Feasibility Study**: We implemented our feasibility study in _DiffDetective_, a library and command-line tool for analysis of edits to preprocessor-based software product lines.
2. **Appendix**: The appendix of our paper is given in PDF format in the file [appendix.pdf][appendix].

## Implementation of Views on Edits to Variational Software in DiffDetective

We implemented the generation of views and our experiment in DiffDetective.
In this section, we outline where our extensions can be found within DiffDetective's code base.

Our implementation of views as proposed in our paper can be found in the packages `org.variantsync.diffdetective.variation.tree.view` and `org.variantsync.diffdetective.variation.diff.view` for views on variation trees and diffs, respectively.
The algorithms _view-naive_ and _view-smart_ from the paper are implemented in the [DiffView](../../src/main/java/org/variantsync/diffdetective/variation/diff/view/DiffView.java) class.

The experiment is implemented in the package `org.variantsync.diffdetective.experiments.views`.
The experiment's entry point is the main method in the [Main](../../src/main/java/org/variantsync/diffdetective/experiments/views/Main.java) class for the experiment.

We documented all relevant source code of our extensions with Javadoc.
The majority of DiffDetective's code base is documented, too.

## Replication of the Feasibility Study

Hardware and software requirements are documented in the [REQUIREMENTS.md](REQUIREMENTS.md) file.
Please make sure that you meet the specified software requirements (there are no specific hardware requirements).

We offer a [Docker](https://www.docker.com/) setup to easily __replicate__ the feasibility study performed in our paper.
Information for the Docker setup and replicating our experiments can be found in the [INSTALL](INSTALL.md) file, including detailed descriptions of each step and troubleshooting advice.

[appendix]: ../../appendix/appendix-splc23.pdf
