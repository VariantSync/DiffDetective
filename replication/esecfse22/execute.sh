#! /bin/bash
if [[ $# -gt 0 ]]; then
echo "Executing $1"
fi
docker run --rm -v "$(pwd)/results":"/home/sherlock/results" diff-detective "$@"
