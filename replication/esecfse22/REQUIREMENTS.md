## Hardware Requirements

None

## Software Requirements

We do not require a certain operating system or prepared environment.
The setup is tested on Windows 10, WSL2, Manjaro, Ubuntu, and MacOS Monterey.

To run DiffDetective, JDK16, and Maven are required.
Dependencies to other packages are documented in the maven build file ([pom.xml](../../pom.xml)) and are handled automatically by Maven.
Alternatively, the docker container can be used on any system supporting docker.
Docker will take care of all requirements and dependencies to replicate our validation.

The requirements to build our `proofs` Haskell library are documented in its respective [proofs/REQUIREMENTS.md](../../proofs/REQUIREMENTS.md) file.

[stack]: https://docs.haskellstack.org/en/stable/README/