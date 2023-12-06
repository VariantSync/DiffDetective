# Explaining Edits to Variability Annotations in Evolving Software Product Lines

This is the replication package for our paper _Explaining Edits to Variability Annotations in Evolving Software Product Lines_.

This replication package consists of two parts:

1. **DiffDetective**: For our validation, we modified [_DiffDetective_](https://github.com/VariantSync/DiffDetective), a java library and command-line tool to classify edits to variability in git histories of preprocessor-based software product lines, to extend the variation diffs constructed by _DiffDetective_ to edge-typed variation diffs we introduce in the paper.
2. **Dataset Overview**: We provide an overview of the 44 inspected datasets with updated links to their repositories in the file [docs/datasets.md][dataset].

## 1. DiffDetective
DiffDetective is a java library and command-line tool to parse and classify edits to variability in git histories of preprocessor-based software product lines by creating [variation diffs][difftree_class] and operating on them.

We offer a [Docker](https://www.docker.com/) setup to easily __replicate__ the validation performed in our paper. 
In the following, we provide a quickstart guide for running the replication.
You can find detailed information on how to install Docker and build the container in the [INSTALL](INSTALL.md) file, including detailed descriptions of each step and troubleshooting advice.

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
> If you want to stop the execution, you can call the provided script for stopping the container in a separate terminal.
> When restarted, the execution will continue processing by restarting at the last unfinished repository.
> If you want to replicate parts of the evaluation on a subset of the datasets, run the replication on a custom dataset (see below for instructions).
> #### Windows:
> `.\stop-execution.bat`
> #### Linux/Mac (bash):
> `./stop-execution.sh`

You might see warnings or errors reported from SLF4J like `Failed to load class "org.slf4j.impl.StaticLoggerBinder"` which you can safely ignore.
Further troubleshooting advice can be found at the bottom of the [Install](INSTALL.md) file.

### 1.3 View the results in the [results][resultsdir] directory
All raw results are stored in the [results][resultsdir] directory.
The aggregated results can be found in the following files.
(Note that the links to the results only have a target _after_ running the replication or verification.)
The results consist of general information about the analysed repositories as well as CSV files with entries for every patch analysed, i.e., every source code file changed in a commit.



## 2. Dataset Overview
We provide an overview of the used 44 open-source preprocessor-based software product lines in the [docs/datasets.md][dataset] file, taken from the original [_DiffDetective_](https://github.com/VariantSync/DiffDetective) implementation.


## 3. Running DiffDetective on Custom Datasets
You can also run DiffDetective on other datasets by providing the path to the dataset file as first argument to the execution script:

#### Windows:
`.\execute.bat path\to\custom\dataset.md`
#### Linux/Mac (bash):
`./execute.sh path/to/custom/dataset.md`

The input file must have the same format as the other dataset files (i.e., repositories are listed in a Markdown table). You can find [dataset files](docs/datasets.md) in the [docs](docs) folder.

[dataset]: docs/datasets.md


[resultsdir]: results

