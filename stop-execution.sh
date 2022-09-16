#! /bin/bash
echo "Stopping Docker container. This will take a moment..."
docker stop "$(docker ps -a -q --filter "ancestor=diff-detective")"
echo "...done."
