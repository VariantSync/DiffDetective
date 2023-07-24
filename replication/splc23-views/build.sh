#!/usr/bin/env bash

# We have to switch to the root directory of the project and build the Docker image from there,
# because Docker only allows access to the files in the current file system subtree (i.e., no access to ancestors).
# We have to do this to get access to 'src', 'docker', 'local-maven-repo', etc.
# For resiliency against different working directories during execution of this
# script we calculate the correct path using the special bash variable
# BASH_SOURCE.
cd "$(dirname "${BASH_SOURCE[0]}")/../.." || exit

docker build -t diff-detective-views -f replication/splc23-views/Dockerfile .
