#!/usr/bin/env bash
echo "Stopping all running replications. This will take a moment..."
docker stop "$(docker ps -a -q --filter "ancestor=diff-detective-views")"
echo "...done."
