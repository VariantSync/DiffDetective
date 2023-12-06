# Installation
## Installation Instructions
In the following, we describe how to replicate the evaluation from our paper step-by-step.
The instructions explain how to build the Docker image and run the validation in a Docker container.

### 1. Start Docker

Start the docker daemon.

### 2. Open a Suitable Terminal
```
# Windows Command Prompt: 
 - Press 'Windows Key + R' on your keyboard
 - Type in 'cmd' 
 - Click 'OK' or press 'Enter' on your keyboard
 
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
git checkout splc23-explanations
```
Finally, navigate to the replication directory
```shell
cd replication/splc23-explanations
```

### 3. Build the Docker Container
To build the Docker container you can run the `build` script corresponding to your operating system:
```
# Windows: 
  .\build.bat
# Linux/Mac (bash): 
  ./build.sh
```

## 4. Replication

### Running the Replication
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
Further troubleshooting advice can be found at the bottom of this file.

The results of the verification will be stored in the [results](../../results) directory.

### Expected Output of the Verification


### (Optional) Running DiffDetective on Custom Datasets
You can also run DiffDetective on other datasets by providing the path to the dataset file as first argument to the execution script:

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

### No results after verification, or 'cannot create directory '../results/validation/current': Permission denied'
`Problem:` This problem can occur due to how permissions are managed inside the Docker container. More specifically, it will appear, if Docker is executed with elevated permissions (i.e., `sudo`) and if there is no [results](../../results) directory because it was deleted manually. In this case, Docker will create the directory with elevated permissions, and the Docker user has no permissions to access the directory.

`Fix:` If there is a _results_ directory, delete it with elevated permission (e.g., `sudo rm -r results`).
Then, create a new _results_ directory without elevated permissions, or execute `git restore .` to restore the deleted directory.

### Failed to load class "org.slf4j.impl.StaticLoggerBinder"
`Problem:` An operation within the initialization phase of the logger library we use (tinylog) failed.

`Fix:` Please ignore this warning. Tinylog will fall back onto a default implementation (`Defaulting to no-operation (NOP) logger implementation`) and logging will work as expected.