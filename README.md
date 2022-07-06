# Classifying Edits to Variability in Source Code

---
TODOS for artifact submission
- create a top level "python" dir
  - put `mining` there
  - put `linegraph` there. Maybe rename `linegraph` to `rendering`
  - put `plotting` there
---

This is the replication package our submission _Classifying Edits to Variability in Source Code_ submitted to the 30th ACM Joint European Software Engineering Conference and Symposium on the Foundations of Software Engineering (ESEC/FSE) in March 2022.

This replication package consists of four parts:

1. **DiffDetective**: For our validation, we built DiffDetective, a java library and command-line tool to classify edits to variability in git histories of preprocessor-based software product lines.
2. **Appendix**: The appendix of our paper is given in PDF format in the file [appendix.pdf](appendix.pdf).
3. **Haskell Formalization**: We provide an extended formalization in the Haskell programming language as described in our appendix. Its implementation can be found in the Haskell project in the [proofs](proofs) directory.
4. **Dataset Overview**: We provide an overview of the 44 inspected datasets with updated links to their repositories in the file [docs/datasets.md][dataset].

## 1. DiffDetective
We offer a [Docker](https://www.docker.com/) setup to easily __replicate__ our validation with _DiffDetective_. 
You can find detailed information on how to install Docker and build the container in the [INSTALL](INSTALL.md) file.
In the following, we provide instructions for running the replication.

### 1.1 Build the Docker container
To build the Docker container you can run the _build_ script corresponding to your OS.
#### Windows: 
`.\build.bat`
#### Linux/Mac (bash): 
`./build.sh`

### 1.2 Start the replication
To execute the replication you can run the _execute_ script corresponding to your OS with `replication` as first argument.

> ! The replication will at least require an hour and might require up to a day depending on your system.
> Therefore, we offer a short verification (5-10 minutes) which runs _DiffDetective_ on only four of the datasets.
> You can run it by providing "verification" as argument instead of "replication" (i.e., `.\execute.bat verification`,  `./execute.sh verification`).
> If you want to stop the replication, you can call the provided script for stopping the container. Note that you will have to restart the entire replication, if you stop it at any point.
> #### Windows:
> `.\stop-execution.bat`
> #### Linux/Mac (bash):
> `./stop-execution.sh`

#### Windows: 
`.\execute.bat replication`
#### Linux/Mac (bash): 
`./execute.sh replication`

### 1.3 View the results in the [results](results) directory
All raw results are stored in the [results](results) directory. The aggregated results can be found in the following files:
- [speed statistics](results/validation/speedstatistics.txt): contains information about the total runtime, median runtime, mean runtime, and more.
- [classification results](results/validation/ultimateresult.metadata.txt): contains information about how often each pattern was found, and more.

(Note that the above links only have a target _after_ running the replication.)

Moreover, the results comprise the (LaTeX) tables that are part of our paper and appendix.

## 2. Appendix

Our appendix consists of:
1. An extended formalization of our concepts in the [Haskell][haskell] programming language. The corresponding source code is also part of this replication package (see below).
2. The proofs for (a) the completeness of variation tree diffs to represent edits to variation trees, and (b) the completeness and unambiguity of our elementary edit patterns.
3. An inspection of edit patterns from related work to show that existing patterns are either composite patterns built from our elementary patterns or similar to our elementary patterns.
4. The complete results of our validation for all 44 datasets.

## 3. Haskell Formalization
The extended formalization is a [Haskell][haskell] library in the [`proofs`](proofs) subdirectory.
Since the `proofs` library is its own software project, we provide a separate documentation of requirements and installation instructions within the projects subdirectory.
Instructions for manually installing Stack are given in [proofs/REQUIREMENTS.md](proofs/REQUIREMENTS.md).
How to build our library and how to run the example is described in the [proofs/INSTALL.md](proofs/INSTALL.md).


## 4. Dataset Overview
### 4.1 Open-Source Repositories
We provide an overview of the used 44 open-source preprocessor-based software product lines in the [docs/datasets.md][dataset] file.
As described in our paper in Section 5.1 this list contains all systems that were studied by Liebig et al., extended by four new subject systems.
We provide updated links for each system's repository.

### 4.2 Forked Repositories for Replication
To guarantee the exact replication of our validation, we created forks of all 44 open-source repositories. The forked repositories are listed in the [replication datasets](docs/replication/datasets.md). These repositories are used when running the replication as described under `1.2`.

## 5. Running DiffDetective on Custom Datasets
You can also run _DiffDetective_ on other datasets by providing the path to the dataset file as first argument to the execution script:

#### Windows:
`.\execute.bat path\to\custon\dataset.md`
#### Linux/Mac (bash):
`./execute.sh path/to/custon/dataset.md`

The input file must have the same format as the other dataset files (i.e., repositories are listed in a Markdown table). You can find [dataset files](docs/datasets.md) in the [docs](docs) folder.

[haskell]: https://www.haskell.org/
[dataset]: docs/datasets.md


