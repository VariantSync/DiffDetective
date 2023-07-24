<img padding="10" align="right" src="https://www.acm.org/binaries/content/gallery/acm/publications/artifact-review-v1_1-badges/artifacts_evaluated_reusable_v1_1.png" alt="ACM Artifacts Evaluated Reusable" width="114" height="113"/>

![Maven](https://github.com/VariantSync/DiffDetective/actions/workflows/maven.yml/badge.svg)
[![Documentation](https://img.shields.io/badge/Documentation-Read-purple)][documentation]
[![Install](https://img.shields.io/badge/Install-Instructions-blue)](INSTALL.md)
[![GitHubPages](https://img.shields.io/badge/GitHub%20Pages-online-blue.svg?style=flat)][website]
[![License](https://img.shields.io/badge/License-GNU%20LGPLv3-blue)](../../LICENSE.LGPL3)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7110095.svg)](https://doi.org/10.5281/zenodo.7110095)

# Classifying Edits to Variability in Source Code

This is the replication package for our paper _Classifying Edits to Variability in Source Code_ accepted at the 30th ACM Joint European Software Engineering Conference and Symposium on the Foundations of Software Engineering (ESEC/FSE 2022).

This replication package consists of four parts:

1. **DiffDetective**: For our validation, we built _DiffDetective_, a java library and command-line tool to classify edits to variability in git histories of preprocessor-based software product lines.
2. **Appendix**: The appendix of our paper is given in PDF format in the file [appendix.pdf][appendix].
3. **Haskell Formalization**: We provide an extended formalization in the Haskell programming language as described in our appendix. Its implementation can be found in the Haskell project in the [proofs](../../proofs) directory.
4. **Dataset Overview**: We provide an overview of the 44 inspected datasets with updated links to their repositories in the file [docs/datasets/all.md][dataset].

## 1. DiffDetective
DiffDetective is a java library and command-line tool to parse and classify edits to variability in git histories of preprocessor-based software product lines by creating [variation diffs][difftree_class] and operating on them.

We offer a [Docker](https://www.docker.com/) setup to easily __replicate__ the validation performed in our paper. 
In the following, we provide a quickstart guide for running the replication.
You can find detailed information on how to install Docker and build the container in the [INSTALL](INSTALL.md) file, including detailed descriptions of each step and troubleshooting advice.

### Prerequisite
All following commands assume that working directory of your terminal is the `esecfse` directory. Please switch directories, if this is not the case:
```shell
cd DiffDetective/replication/esecfse22
```

### 1.1 Build the Docker container
Start the docker deamon.
Clone this repository.
Open a terminal and navigate to the root directory of this repository.
To build the Docker container you can run the `build` script corresponding to your operating system.
#### Windows: 
`.\build.bat`
#### Linux/Mac (bash): 
`./build.sh`

### 1.2 Start the replication
To execute the replication you can run the `execute` script corresponding to your operating system with `replication` as first argument.

#### Windows:
`.\execute.bat replication`
#### Linux/Mac (bash):
`./execute.sh replication`

> WARNING!
> The replication will at least require an hour and might require up to a day depending on your system.
> Therefore, we offer a short verification (5-10 minutes) which runs DiffDetective on only four of the datasets.
> You can run it by providing "verification" as argument instead of "replication" (i.e., `.\execute.bat verification`,  `./execute.sh verification`).
> If you want to stop the execution, you can call the provided script for stopping the container in a separate terminal.
> When restarted, the execution will continue processing by restarting at the last unfinished repository.
> #### Windows:
> `.\stop-execution.bat`
> #### Linux/Mac (bash):
> `./stop-execution.sh`

You might see warnings or errors reported from SLF4J like `Failed to load class "org.slf4j.impl.StaticLoggerBinder"` which you can safely ignore.
Further troubleshooting advice can be found at the bottom of the [Install](INSTALL.md) file.

### 1.3 View the results in the [results][resultsdir] directory
All raw results are stored in the [results][resultsdir] directory.
The aggregated results can be found in the following files.
(Note that the links below only have a target _after_ running the replication or verification.)
- [speed statistics][resultsdir_speed_statistics]: contains information about the total runtime, median runtime, mean runtime, and more.
- [classification results][resultsdir_classification_results]: contains information about how often each class was found, and more.

Moreover, the results comprise the (LaTeX) tables that are part of our paper and appendix.

### Documentation

DiffDetective is documented with javadoc. The documentation can be accessed on this [website][documentation]. Notable classes of our library are:
- [DiffTree](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/diff/difftree/DiffTree.html) and [DiffNode](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/diff/difftree/DiffNode.html) implement variation diffs from our paper. A variation diff is represented by an instance of the `DiffTree` class. It stores the root node of the diff and offers various methods to parse, traverse, and analyze variation diffs. `DiffNode`s represent individual nodes within a variation diff.
- [EditClassValidation](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/validation/Validation.html) contains the main method for our validation.
- [ProposedEditClasses](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/editclass/proposed/ProposedEditClasses.html) holds the catalog of the nine edit classes we proposed in our paper. It implements the interface [EditClassCatalogue](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/editclass/EditClassCatalogue.html), which allows to define custom edit classifications.
- [BooleanAbstraction](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/feature/BooleanAbstraction.html) contains data and methods for boolean abstraction of higher-order logic formulas. We use this for macro parsing.
- [GitDiffer](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/diff/GitDiffer.html) may parse the history of a git repository to variation diffs.
- The [datasets](https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/datasets/package-summary.html) package contains various classes for describing and loading datasets.

## 2. Appendix

Our [appendix][appendix] consists of:
1. An extended formalization of our concepts in the [Haskell][haskell] programming language. The corresponding source code is also part of this replication package (see below).
2. The proofs for (a) the completeness of variation diffs to represent edits to variation trees, and (b) the completeness and unambiguity of our edit classes.
3. An inspection of edit patterns from related work to show that existing patterns are either composite patterns built from our edit classes or similar to one of our edit classes. The used diffs of these patterns can also be found in [docs/compositepatterns](../../docs/compositepatterns).
4. The complete results of our validation for all 44 datasets.

## 3. Haskell Formalization
The extended formalization is a [Haskell][haskell] library in the [`proofs`](../../proofs) subdirectory.
Since the `proofs` library is its own software project, we provide a separate documentation of requirements and installation instructions within the projects subdirectory.
Requirements and instructions for setting up the build environment (Stack) are given in [proofs/REQUIREMENTS.md](../../proofs/REQUIREMENTS.md).
How to build our library and how to run the example is described in the [proofs/INSTALL.md](../../proofs/INSTALL.md).


## 4. Dataset Overview
### 4.1 Open-Source Repositories
We provide an overview of the used 44 open-source preprocessor-based software product lines in the [docs/datasets/all.md][dataset] file.
As described in our paper in Section 5.1, this list contains all systems that were studied by Liebig et al., extended by four new subject systems (Busybox, Marlin, LibSSH, Godot).
We provide updated links for each system's repository.

### 4.2 Forked Repositories for Replication
To guarantee the exact replication of our validation, we created forks of all 44 open-source repositories at the state we performed the validation for our paper.
The forked repositories are listed in the [replication datasets](../../docs/datasets/esecfse22-replication.md) and are located at the Github user profile [DiffDetective](https://github.com/DiffDetective?tab=repositories).
These repositories are used when running the replication as described under `1.2` and in the [INSTALL](INSTALL.md).

## 5. Running DiffDetective on Custom Datasets
You can also run DiffDetective on other datasets by providing the path to the dataset file as first argument to the execution script:

#### Windows:
`.\execute.bat path\to\custom\dataset.md`
#### Linux/Mac (bash):
`./execute.sh path/to/custom/dataset.md`

The input file must have the same format as the other dataset files (i.e., repositories are listed in a Markdown table). You can find [dataset files](../../docs/datasets/all.md) in the [docs/datasets](../../docs/datasets) folder.

[difftree_class]: https://variantsync.github.io/DiffDetective/docs/javadoc/org/variantsync/diffdetective/diff/difftree/DiffTree.html
[haskell]: https://www.haskell.org/
[dataset]: ../../docs/datasets/all.md
[appendix]: ../../appendix/appendix-esecfse22.pdf

[documentation]: https://variantsync.github.io/DiffDetective/docs/javadoc/
[website]: https://variantsync.github.io/DiffDetective/

[resultsdir]: results
[resultsdir_classification_results]: results/validation/current/ultimateresult.metadata.txt
[resultsdir_speed_statistics]: results/validation/current/speedstatistics.txt
