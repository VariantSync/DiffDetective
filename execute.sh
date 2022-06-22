#! /bin/bash
echo "Starting $1"
docker run --rm -v "$(pwd)/results":"/home/sherlock/results" replication-package "$@"

echo "Done."
