#! /bin/bash
# Assure that the script is only called from the splc23-views folder
current_dir=$(pwd)
expected_path="/replication/splc23-views"
if [[ ! $current_dir =~ $expected_path ]]; then
  echo "error: the script must be run from inside the splc23-views directory, i.e., DiffDetective$expected_path"
  exit 1
fi

if [[ $# -gt 0 ]]; then
echo "Executing $1"
fi
docker run --rm -v "$(pwd)/results":"/home/sherlock/results" diff-detective-views "$@"
