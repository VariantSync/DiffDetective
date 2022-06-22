#!/bin/sh
ls -l
if [ "$(id -u)" = "0" ]; then
  # running on a developer laptop as root
  fix-perms -r -u user -g user /home/user
  exec gosu user "$@"
else
  # running in production as a user
  exec "$@"
fi