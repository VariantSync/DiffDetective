# Replication

The script `create-forks-on-github.sh` can be used to create forks of all the
repositories in the `../../../DiffDetectiveMining/` folder. It marks them as a
fork and replicates the local state in this fork. The indended use case is to
provide immutable copies of all source data to make all results of this research
reproducible.

## Usage
First run:
```bash
./create-forks-on-github.sh
```

It will ask some verification questions and prints all commands that it will
perform. If the source repositories are not located in
`../../../DiffDetectiveMining/` (relative to this file) or
`../DiffDetectiveMining` (relative to the root directory of this repository) the
comment character of the variable `PATH_TO_REPOSITORIES` in the second paragraph
can be removed and the actual path can be set.

To execute the printed commands uncomment the last line of the second paragraph
which disables `DRY_RUN`.

---
Warning: This script doesn't properly replicate the repositories `libxml2` and
`gcc`. To manually finish the replication process for these repositories, run
the following in the `DiffDetectiveMining` directory (sibling of the
`DiffDetective` repository manually:
```
for repo in libxml2 gcc
do
  pushd "$repo"
  git remote add origin "git@github.com:DiffDetective/$repo.git"
  git push -f origin
  popd
done
```

## Login
You can either login beforehand with `gh auth login` or just run the script
which will run this command for you. This script will *not* log you out once
it's finished because you may want to run additional commands (or you where
previously logged in).

## Additional notes
This script should be idempotent as `gh` detects an already forked project and
the performed force push should not change anything (unless the remote
repository was changed).

## Requirements
This script requires a standard UNIX environment with at least the following
additional tools:
- `bash`, the GNU POSIX shell
- `gh`, the github command line interface
- GNU `find`, the POSIX `find` with some additional options (mainly `-print0`,
  but this was not tested with other `find` implementations)

This script was tested under GNU+Linux.
