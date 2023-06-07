## Hardware Requirements

None

## Software Requirements

We do not require a certain operating system or prepared environment.
The setup is tested on Windows 10, WSL2, Manjaro, Ubuntu, and MacOS Monterey.

To run DiffDetective, JDK16, and Maven are required.
Dependencies to other packages are documented in the maven build file ([pom.xml](../../pom.xml)) and are handled automatically by Maven.
Our replication is based on a docker container that can be used on any system supporting docker.
Docker will take care of all requirements and dependencies to replicate our feasibility study.

How to install Docker depends on your operating system:

- _Windows or Mac_: You can find download and installation instructions [here](https://www.docker.com/get-started).
- _Linux Distributions_: How to install Docker on your system, depends on your distribution. The chances are high that Docker is part of your distributions package database.
  Docker's [documentation](https://docs.docker.com/engine/install/) contains instructions for common distributions.