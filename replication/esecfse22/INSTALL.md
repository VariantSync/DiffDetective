# Installation
## Installation Instructions
In the following, we describe how to replicate the validation from our paper (Section 5) step-by-step.
The instructions explain how to build the Docker image and run the validation in a Docker container.

### 1. Install Docker (if required)
How to install Docker depends on your operating system:

- _Windows or Mac_: You can find download and installation instructions [here](https://www.docker.com/get-started).
- _Linux Distributions_: How to install Docker on your system, depends on your distribution. The chances are high that Docker is part of your distributions package database.
Docker's [documentation](https://docs.docker.com/engine/install/) contains instructions for common distributions.

Then, start the docker deamon.

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
Then, navigate to the `esecfse22` folder in your local clone of this repository:
```shell
cd DiffDetective/replication/esecfse22
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
To execute the replication you can run the `execute` script corresponding to your operating system with `replication` as first argument. To execute the script you first have to navigate to the `esecfse22` directory, if you have not done so. 
```shell
cd DiffDetective/replication/esecfse22
```

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
Further troubleshooting advice can be found at the bottom of this file.

The results of the verification will be stored in the [results](results) directory.

### Expected Output of the Verification
The aggregated results of the verification/replication can be found in the following files.
The example file content shown below should match your results when running the _verification_.
(Note that the links below only have a target _after_ running the replication or verification.)

- The [speed statistics](results/validation/current/speedstatistics.txt) contain information about the total runtime, median runtime, mean runtime, and more:
  ```
  #Commits: 24701
  Total   commit process time is: 14.065916666666668min
  Fastest commit process time is: d86e352859e797f6792d6013054435ae0538ef6d___xfig___0ms
  Slowest commit process time is: 9838b7032ea9792bec21af424c53c07078636d21___xorg-server___7996ms
  Median  commit process time is: f77ffeb9b26f49ef66f77929848f2ac9486f1081___tcl___13ms
  Average commit process time is: 34.166835350795516ms
  ```
- The [classification results](results/validation/current/ultimateresult.metadata.txt) contain information about how often each pattern was matched, and more.
  ```
  repository: <NONE>
  total commits: 42323
  filtered commits: 7425
  failed commits: 0
  empty commits: 10197
  processed commits: 24701
  tree diffs: 80751
  fastestCommit: 518e205b06d0dc7a0cd35fbc2c6a4376f2959020___xorg-server___0ms
  slowestCommit: 9838b7032ea9792bec21af424c53c07078636d21___xorg-server___7996ms
  runtime in seconds: 853.9739999999999
  runtime with multithreading in seconds: 144.549
  treeformat: org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat
  nodeformat: org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat
  edgeformat: org.variantsync.diffdetective.mining.formats.DirectedEdgeLabelFormat with org.variantsync.diffdetective.mining.formats.ReleaseMiningDiffNodeFormat
  analysis: org.variantsync.diffdetective.validation.PatternValidationTask
  #NON nodes: 0
  #ADD nodes: 0
  #REM nodes: 0
  filtered because not (is not empty): 212
  AddToPC: { total = 443451; commits = 22470 }
  AddWithMapping: { total = 51036; commits = 2971 }
  RemFromPC: { total = 406809; commits = 21384 }
  RemWithMapping: { total = 36622; commits = 2373 }
  Specialization: { total = 7949; commits = 1251 }
  Generalization: { total = 11057; commits = 955 }
  Reconfiguration: { total = 3186; commits = 381 }
  Refactoring: { total = 4862; commits = 504 }
  Untouched: { total = 0; commits = 0 }
  #Error[conditional macro without expression]: 2
  #Error[#else after #else]: 2
  #Error[#else or #elif without #if]: 11
  #Error[#endif without #if]: 12
  #Error[not all annotations closed]: 8
  ```

Moreover, the results comprise the (LaTeX) tables that are part of our paper and appendix.
The processing times might deviate because performance depends on your hardware.

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
`Problem:` This problem can occur due to how permissions are managed inside the Docker container. More specifically, it will appear, if Docker is executed with elevated permissions (i.e., `sudo`) and if there is no [results](results) directory because it was deleted manually. In this case, Docker will create the directory with elevated permissions, and the Docker user has no permissions to access the directory.

`Fix:` If there is a _results_ directory, delete it with elevated permission (e.g., `sudo rm -r results`). 
Then, create a new _results_ directory without elevated permissions, or execute `git restore .` to restore the deleted directory.

### Failed to load class "org.slf4j.impl.StaticLoggerBinder"
`Problem:` An operation within the initialization phase of the logger library we use (tinylog) failed.

`Fix:` Please ignore this warning. Tinylog will fall back onto a default implementation (`Defaulting to no-operation (NOP) logger implementation`) and logging will work as expected.