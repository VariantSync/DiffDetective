#! /bin/bash
echo "Starting $1"
docker run --rm -v "$(pwd)/docker-output":"/home/user/output" replication-package "$@"

echo "Done."