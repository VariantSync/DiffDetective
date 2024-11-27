![Maven](https://github.com/VariantSync/DiffDetective/actions/workflows/maven.yml/badge.svg)
[![Documentation](https://img.shields.io/badge/Documentation-Read-purple)][documentation]
[![Install](https://img.shields.io/badge/Install-Instructions-blue)](INSTALL.md)
[![GitHubPages](https://img.shields.io/badge/GitHub%20Pages-online-blue.svg?style=flat)][website]
[![License](https://img.shields.io/badge/License-GNU%20LGPLv3-blue)](../../LICENSE.LGPL3)

# Unparsing Experiment
This is an experiment for the bachelor thesis by Eugen Shulimov which tests the unparser for variation trees and diffs.

### Prerequisite
All following commands assume that working directory of your terminal is the `thesis-es` directory. Please switch directories, if this is not the case:
```shell
cd DiffDetective/replication/thesis-es
```

### Build the Docker container
Start the docker deamon.
Clone this repository.
Open a terminal and navigate to the root directory of this repository.
To build the Docker container you can run the `build` script corresponding to your operating system.
#### Windows:
`.\build.bat`
#### Linux/Mac (bash):
`./build.sh`

### Start the experiment
To execute the experiment you can run the `execute`script corresponding to your operating system.

#### Windows:
`.\execute.bat
#### Linux/Mac (bash):
`./execute.sh

> If you want to stop the execution, you can call the provided script for stopping the container in a separate terminal.
> When restarted, the execution will continue processing by restarting at the last unfinished repository.
> #### Windows:
> `.\stop-execution.bat`
> #### Linux/Mac (bash):
> `./stop-execution.sh`

You might see warnings or errors reported from SLF4J like `Failed to load class "org.slf4j.impl.StaticLoggerBinder"` which you can safely ignore.

### View the results in the [results][resultsdir] directory
All raw results are stored in the [results][resultsdir] directory.

[documentation]: https://variantsync.github.io/DiffDetective/docs/javadoc/
[website]: https://variantsync.github.io/DiffDetective/

[resultsdir]: results
