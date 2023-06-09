#!/usr/bin/env bash
# Assure that the script is only called from the splc23-views folder
current_dir=$(pwd)
expected_path="/replication/splc23-views"
if [[ ! $current_dir =~ $expected_path ]]; then
  echo "error: the script must be run from inside the splc23-views directory, i.e., DiffDetective$expected_path"
  exit 1
fi

# We have to switch to the root directory of the project and build the Docker image from there,
# because Docker only allows access to the files in the current file system subtree (i.e., no access to ancestors).
# We have to do this to get access to 'src', 'docker', 'local-maven-repo', etc.
cd ../..

docker build -t diff-detective-views -f replication/splc23-views/Dockerfile .
