#!/usr/bin/env bash

cd "$(dirname "${BASH_SOURCE[0]}")/.."

# extracts the first version tag in pom.xml
sed -n '/<version/ {s/.*version>\(.*\)<\/version.*/\1/; p; q}' pom.xml
