

# Explaining Edits to Variability Annotations in Evolving Software Product Lines

This is the replication package for our paper _Explaining Edits to Variability Annotations in Evolving Software Product Lines_.

This replication package consists of two parts:

1. **DiffDetective**: For our validation, we built _DiffDetective_, a java library and command-line tool to classify edits to variability in git histories of preprocessor-based software product lines.
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
(Note that the links below only have a target _after_ running the replication or verification.)



## 2. Dataset Overview
### 2.1 Open-Source Repositories
We provide an overview of the used 44 open-source preprocessor-based software product lines in the [docs/datasets.md][dataset] file.
As described in our paper in Section 5.1, this list contains all systems that were studied by Liebig et al., extended by four new subject systems (Busybox, Marlin, LibSSH, Godot).
We provide updated links for each system's repository.

### 2.2 Forked Repositories for Replication
To guarantee the exact replication of our validation, we created forks of all 44 open-source repositories at the state we performed the validation for our paper.
The forked repositories are listed in the [replication datasets](docs/replication/datasets.md) and are located at the Github user profile [DiffDetective](https://github.com/DiffDetective?tab=repositories).
These repositories are used when running the replication as described under `1.2` and in the [INSTALL](INSTALL.md).

## 3. Running DiffDetective on Custom Datasets
You can also run DiffDetective on other datasets by providing the path to the dataset file as first argument to the execution script:

#### Windows:
`.\execute.bat path\to\custom\dataset.md`
#### Linux/Mac (bash):
`./execute.sh path/to/custom/dataset.md`

The input file must have the same format as the other dataset files (i.e., repositories are listed in a Markdown table). You can find [dataset files](docs/datasets.md) in the [docs](docs) folder.

[dataset]: docs/datasets.md


[resultsdir]: results
