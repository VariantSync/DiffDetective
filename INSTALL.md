# Installation
## Installation Instructions
In the following, we describe how to build the Docker image and run the experiments in Docker containers.

### Install Docker (if required)
How to install Docker depends on your operating system.

#### Windows or Mac
You can find download and installation instructions [here](https://www.docker.com/get-started).

#### Linux Distributions
How to install Docker on your system, depends on your distribution. However, the chances are high that Docker is part of your distributions package database.
Docker's [documentation](https://docs.docker.com/engine/install/) contains instructions for common distributions.

### Open a Suitable Terminal
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

### Build the Docker Container
To build the Docker container you can run the build script corresponding to your OS
```
# Windows: 
  .\build.bat
# Linux/Mac (bash): 
  ./build.sh
```

## Verification & Expected Output

### Running the Verification
To run the verification you can run the script corresponding to your OS with `verification` as first argument. The verification should take about 10-20 minutes depending on your hardware.
```
# Windows: 
  .\execute.bat verification
# Linux/Mac (bash): 
  ./execute.sh verification
```
The results of the verification will be stored in the [results](results) directory.

### Expected Output of the Verification
The aggregated results of the verification can be found in the following files.

- The [speed statistics](results/validation/speedstatistics.txt) contain information about the total runtime, median runtime, mean runtime, and more:
  ```
  #Commits: 14527
  Total   commit process time is: 12.427866666666667min
  Fastest commit process time is: df4a1fa9c5cc5d54a9347a2bf4843cae87a942f1___xorg-server___0ms
  Slowest commit process time is: 9838b7032ea9792bec21af424c53c07078636d21___xorg-server___14578ms
  Median  commit process time is: 6dc71f6b2c7ff49adb504426b4cd206e4745e1e3___xorg-server___19ms
  Average commit process time is: 51.330075032697735ms
  ```
- The [classification results](results/validation/ultimateresult.metadata.txt) contain information about how often each pattern was found, and more.
  ```
  repository: <NONE>
  total commits: 18046
  filtered commits: 593
  failed commits: 0
  empty commits: 2926
  processed commits: 14527
  tree diffs: 55008
  fastestCommit: df4a1fa9c5cc5d54a9347a2bf4843cae87a942f1___xorg-server___0ms
  slowestCommit: 9838b7032ea9792bec21af424c53c07078636d21___xorg-server___14578ms
  runtime in seconds: 747.5400000000001
  runtime with multithreading in seconds: 137.22
  treeformat: diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat
  nodeformat: mining.formats.ReleaseMiningDiffNodeFormat
  edgeformat: mining.formats.DirectedEdgeLabelFormat with mining.formats.ReleaseMiningDiffNodeFormat
  analysis: mining.strategies.PatternValidation
  #NON nodes: 0
  #ADD nodes: 0
  #REM nodes: 0
  filtered because not (is not empty): 132
  AddToPC: { total = 260536; commits = 12703 }
  AddWithMapping: { total = 27720; commits = 1447 }
  RemFromPC: { total = 235017; commits = 11830 }
  RemWithMapping: { total = 15381; commits = 1361 }
  Specialization: { total = 4662; commits = 624 }
  Generalization: { total = 7397; commits = 564 }
  Reconfiguration: { total = 2231; commits = 258 }
  Refactoring: { total = 5769; commits = 921 }
  Untouched: { total = 0; commits = 0 }
  #Error[#else after #else]: 2
  #Error[#endif without #if]: 8
  #Error[#else or #elif without #if]: 9
  #Error[not all annotations closed]: 6
  ```
  
(Note that the above links only have a target after running the verification.)
The processing times might deviate.

## Troubleshooting

### 'Got permission denied while trying to connect to the Docker daemon socket'
`Problem:` This is a common problem under Linux, if the user trying to execute Docker commands does not have the permissions to do so. 

`Fix:` You can fix this problem by either following the [post-installation instructions](https://docs.docker.com/engine/install/linux-postinstall/), or by executing the scripts in the replication package with elevated permissions (i.e., `sudo`)

### 'Unable to find image 'replication-package:latest' locally'
`Problem:` The Docker container could not be found. This either means that the name of the container that was built does not fit the name of the container that is being executed (this only happens if you changed the provided scripts), or that the Docker container was not built yet. 

`Fix:` Follow the instructions described above in the section `Build the Docker Container`.

### No results after verification, or 'cannot create directory '../results/difftrees': Permission denied'
`Problem:` This problem can occur due to how permissions are managed inside the Docker container. More specifically, it will appear, if Docker is executed with elevated permissions (i.e., `sudo`) and if there is no [results](results) directory because it was deleted manually. In this case, Docker will create the directory with elevated permissions, and the Docker user has no permissions to access the directory.

`Fix:` If there is a _results_ directory delete it with elevated permission (e.g., `sudo rm -r results`). 
Then, create a new _results_ directory without elevated permissions, or execute `git restore .` to restore the deleted directory.
