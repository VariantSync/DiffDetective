#! /bin/bash
echo "Stopping Docker container. This will take a moment..."
docker stop "$(docker ps -a -q --filter "ancestor=replication-package")"
echo "...done."