#!/usr/bin/env bash

# Default settings
PATH_TO_REPOSITORIES="$(dirname "${BASH_SOURCE[0]}")/../../../DiffDetectiveMining"
DRY_RUN=y

# Override settings
#PATH_TO_REPOSITORIES="absolute/path/to/directory/containing/the/respositories"
# uncomment to following line, to actually run th 'gh' commands
#DRY_RUN=n

continue-with() {
  echo
  read -p "Do you want to continue with $1? [y/N] " answer
  [ "$answer" == "y" ]
}

run() {
  echo "\$ $*"
  if [ "$DRY_RUN" = "n" ]
  then
    "$@"
  fi
}

repos() {
  find "$PATH_TO_REPOSITORIES" -mindepth 1 -maxdepth 1 -type d "$@"
}

PATH_TO_REPOSITORIES="$(realpath "$PATH_TO_REPOSITORIES")"
cd "$PATH_TO_REPOSITORIES"
echo "The following repos in '$PATH_TO_REPOSITORIES' will be forked:"
repos
continue-with "these $(repos -print0 | tr -d -c '\0' | tr '\0' '\n' | wc -l) repos" || exit 1

if gh auth status |& grep -q 'You are not logged into any GitHub hosts.' &>/dev/null
then
  run gh auth login || exit 1
  was_logged_in=0
else
  echo
  gh auth status

  continue-with "this account" ||
  {
    run gh auth logout &&
    run gh auth login || exit 1
  }
  was_logged_in=1
fi

repos -print0 |
while IFS= read -d '' -r repository
do
  echo
  run cd "$repository"
  run gh repo fork --remote
  run git push -f origin
done

if [ "$was_logged_in" = "1" ]
then
  cat <<EOF

Warning: 'gh' is still logged in, to log out use

    gh auth logout

EOF
else
  echo
  run gh auth logout
fi
