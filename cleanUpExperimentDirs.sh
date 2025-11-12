#!/usr/bin/env bash
set -euo pipefail

# Usage: ./delete_experiments.sh [START_DIR]
# Wenn kein START_DIR angegeben wird, wird das aktuelle Verzeichnis (.) verwendet.
START_DIR="${1:-.}"

# Schalte nullglob, damit das Array leer ist, falls nichts passt
shopt -s nullglob

# Finde nur die Unterverzeichnisse auf Ebene 1
mapfile -t EXP_DIRS < <(find "$START_DIR" -maxdepth 1 -mindepth 1 -type d -name 'experiment*' -print)

if [ ${#EXP_DIRS[@]} -eq 0 ]; then
  echo "Keine 'experiment*'-Verzeichnisse in '$START_DIR' gefunden."
  exit 0
fi

echo "Folgende Verzeichnisse werden gelöscht (inkl. Inhalt):"
for d in "${EXP_DIRS[@]}"; do
  echo "  $d"
done

read -p "Willst du wirklich alle diese Verzeichnisse löschen? [j/N] " answer
case "$answer" in
  [jJ]* )
    echo "Starte Löschung..."
    rm -rf "${EXP_DIRS[@]}"
    echo "Löschvorgang abgeschlossen."
    ;;
  * )
    echo "Abgebrochen."
    exit 1
    ;;
esac

