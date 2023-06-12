# Installation
## Installation Instructions
In the following, we describe how to replicate the feasibility study from our paper (Section 6) step-by-step.
The instructions explain how to build the Docker image and run the validation in a Docker container.

### 1. Start Docker

Start the docker deamon:

- **On Linux**: Typically, the docker deamon runs automatically. Otherwise, run `sudo systemctl start docker`.
- **On Windows**: Open the search bar using the 'Windows Key' and search for 'Docker' or 'Docker Desktop'.

More detailed instructions on starting the deamon are given [here](https://docs.docker.com/config/daemon/start/) on the docker website. 

### 2. Open a Suitable Terminal
```
# Windows PowerShell:
 - Open the search bar (Default: 'Windows Key') and search for 'PowerShell'
 - Start the PowerShell
 
# Linux:
 - Press 'ctrl + alt + T' on your keyboard
```

Clone this repository to a directory of your choice using git:
```shell
git clone https://github.com/VariantSync/DiffDetective.git
```
Then, navigate to the root of your local clone of this repository
```shell
cd DiffDetective
```
and checkout the branch of the replication:
```shell
git checkout splc23-views
```
Finally, navigate to the replication directory
```shell
cd replication/splc23-views
```

### 3. Build the Docker Container
To build the Docker container you can run the `build` script corresponding to your operating system:
```
# Windows: 
  .\build.bat
# Linux/Mac (bash): 
  ./build.sh
```

## 4. Verification & Replication

### Running the Replication or Verification
To execute the replication you can run the `execute` script corresponding to your operating system with `replication` as first argument.

#### Windows:
`.\execute.bat replication`
#### Linux/Mac (bash):
`./execute.sh replication`

> WARNING!
> The replication may require multiple hours, depending on your system (and internet connection to clone the datasets repositories).
> Therefore, we offer a short verification (5-10 minutes) which runs the feasibility study on only four of the datasets (instead of all 44).
> You can run the short verification by providing "verification" as argument instead of "replication" (i.e., `.\execute.bat verification`,  `./execute.sh verification`).
> If you want to stop the execution, you can call the provided script for stopping the container in a separate terminal.
> When restarted, the execution will continue processing by restarting at the last unfinished repository.
> #### Windows:
> `.\stop-execution.bat`
> #### Linux/Mac (bash):
> `./stop-execution.sh`

You might see warnings or errors reported from SLF4J like `Failed to load class "org.slf4j.impl.StaticLoggerBinder"` which you can safely ignore.
Further **troubleshooting advice** can be found at the bottom of this file.

The results of the verification will be stored in the [results](results) directory.

### Expected Output of the Feasibility Study
The aggregated results of the study can be found in the `results/views` directory.
The results split into two subdirectories:

#### results/views/current

There should be a subdirectory for each repository, the feasibility study has been executed on.
The analysis processes the commits of each repository in batches of up to 1000 commits.
Each repository directory contains three files for each batch.
The three files are named by the hash of the first commit in the batch.
The files are:

- `<first commit hash>.metadata.txt`: contains various metadata on the analysis of this commit batch, such as the number of processed commits.
- `<first commit hash>.committimes.txt`: Contains the time in milliseconds each commit in the batch required to be analysed.
- `<first commit hash>.views.csv`: contains information on each generated view. This file contains the main results of our feasibility study.

Additionally, each repository directory also contains a `totalresult.metadata.txt`, which is an aggregation of the `*.metadata.txt` files of each batch.

#### results/views/current_plots

The directory `results/views/current_plots` contains plots, latex tables, and aggregated results that we used to report results in the paper. The directory should contain the following files:
- `count.csv`: Lists how many views of each view type were generated.
- `hist.tex`: A latex table that reports the runtimes of the $view_{smart}$ and $view_{naive}$ algorithm. Table 1 in our paper is a slightly adapted variant of `hist.tex`.
- `median.tex`: A latex table that reports the median runtime for each view type per algorithm.
- `merged.csv`: An aggregation of all `*.views.csv` of all batches of all repositories within the `results/views/current` directory. This file thus lists the result data for each view we generated during the entire feasibility study.
- `rank.csv`: Lists the slowest view generations with the naive algorithm. This table allows us to inspect how the $view_{smart}$ algorithm performs on the worst case candidates of the $view_{naive}$ algorithm.
- `rel_speedup1sOrMore.csv`: Shows the relative speedups with the optimized algorithm $view_{smart}$ relative to the naive algorithm $view_{naive}$ for all views that required 1s or longer to generate with $view_{naive}$.
- `wilcoxon.csv`: Results of the Wilcoxon Signed-Rank Test for determining whether there is a statistically significant improvement in runtimes with the optimized algorithm $view_{smart}$.

### (Optional) Running the Feasibility Study on Custom Datasets
You can also run the feasibility study on other datasets by providing the path to the dataset file as first argument to the execution script:

#### Windows:
`.\execute.bat path\to\custom\dataset.md`
#### Linux/Mac (bash):
`./execute.sh path/to/custom/dataset.md`

The input file must have the same format as the other dataset files (i.e., repositories are listed in a Markdown table). You can find [dataset files](../../docs/datasets/all.md) in the [docs/datasets](../../docs/datasets) folder.

## Troubleshooting

### 'Got permission denied while trying to connect to the Docker daemon socket'
`Problem:` This is a common problem under Linux, if the user trying to execute Docker commands does not have the permissions to do so. 

`Fix:` You can fix this problem by either following the [post-installation instructions](https://docs.docker.com/engine/install/linux-postinstall/), or by executing the scripts in the replication package with elevated permissions (i.e., `sudo`).

### 'Unable to find image 'replication-package:latest' locally'
`Problem:` The Docker container could not be found. This either means that the name of the container that was built does not fit the name of the container that is being executed (this only happens if you changed the provided scripts), or that the Docker container was not built yet. 

`Fix:` Follow the instructions described above in the section `Build the Docker Container`.

### No results after verification, or 'cannot create directory '../results/views/current': Permission denied'
`Problem:` This problem can occur due to how permissions are managed inside the Docker container. More specifically, it will appear, if Docker is executed with elevated permissions (i.e., `sudo`) and if there is no [results](results) directory because it was deleted manually. In this case, Docker will create the directory with elevated permissions, and the Docker user has no permissions to access the directory.

`Fix:` If there is a _results_ directory, delete it with elevated permission (e.g., `sudo rm -r results`). 
Then, create a new _results_ directory without elevated permissions, or execute `git restore .` to restore the deleted directory.

### Failed to load class "org.slf4j.impl.StaticLoggerBinder"
`Problem:` An operation within the initialization phase of the logger library we use (tinylog) failed.

`Fix:` Please ignore this warning. Tinylog will fall back onto a default implementation (`Defaulting to no-operation (NOP) logger implementation`) and logging will work as expected.

### './build.sh: Bad substitution' or './execute.sh: [[: not found'

`Problem:` The scripts `build.sh` and `execute.sh` were not run in bash. The scripts were probably run in shell explicitly (i.e., the `sh` command, for example via `sh build.sh`).

`Fix:` Run the scripts directly (i.e., `./build.sh` or `./execute.sh ...`) or via bash (i.e., `bash build.sh`).