#!/usr/bin/env bash
# Assure that the script is only called from the esecfse22 folder
current_dir=$(pwd)
expected_path="/replication/esecfse22"
if [[ ! $current_dir =~ $expected_path ]]; then
  echo "error: the script must be run from inside the esecfse22 directory, i.e., DiffDetective$expected_path"
  exit 1
fi

if [[ $# -gt 0 ]]; then
echo "Executing $1"
fi
docker run --rm -v "$(pwd)/results":"/home/sherlock/results" diff-detective "$@"
