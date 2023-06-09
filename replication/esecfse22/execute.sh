#!/usr/bin/env bash
# Assure that the script is only called from the esecfse22 folder
cd "$(dirname "${BASH_SOURCE[0]}")/../.." || exit

if [[ $# -gt 0 ]]; then
echo "Executing $1"
fi
docker run --rm -v "$(pwd)/results":"/home/sherlock/results" diff-detective "$@"
