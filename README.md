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
2. The proofs for (a) the completeness of variation tree diffs to represent edits to variation trees, and (b) the completeness and unambiguity of our elementary edit patterns.
3. An inspection of edit patterns from related work to show that existing patterns are either composite patterns built from our elementary patterns or similar to our elementary patterns.
4. The complete results of our validation for all 44 datasets.

## DiffDetective
We offer a [Docker](https://www.docker.com/) setup to easily __replicate__ our validation with _DiffDetective_. 
You can find detailed information on how to install Docker and build the container in the [INSTALL](INSTALL.md) file.

### 1. Build the Docker container
To build the Docker container you can run the _build_ script corresponding to your OS.
#### Windows: 
`.\build.bat`
#### Linux/Mac (bash): 
`./build.sh`

### 2. Start the replication
To execute the replication you can run the _execute_ script corresponding to your OS with `replication` as first argument.

> The replication will at least require several hours and might require up to a few days depending on your system.
> Therefore, we offer a short validation (5-10 minutes) which runs _DiffDetective_ on only four of the datasets.
> You can run it by providing "validation" as argument instead of "replication" (i.e., ./execute.sh validation).

#### Windows: 
`.\execute.bat replication`
#### Linux/Mac (bash): 
`./execute.sh replication`



### 3. View the results in the [results](results) directory
All raw results are stored in the [results](results) directory. The aggregated results can be found in the following files:
- [speed statistics](results/difftrees/speedstatistics.txt): contains information about the total runtime, median runtime, mean runtime, and more.
- [classification results](results/difftrees/ultimateresult.metadata.txt): contains information about how often each pattern was found, and more.

Moreover, the results comprise the (LaTeX) tables that are part of our paper and appendix.

## Haskell Formalization
The extended formalization in Haskell is a library using the _Stack_ build system.

Instructions for manually installing Stack are given in [proofs/REQUIREMENTS.md](proofs/REQUIREMENTS.md).
How to build our library and how to run the example is described in the [proofs/INSTALL.md](proofs/INSTALL.md).

[haskell]: https://www.haskell.org/
