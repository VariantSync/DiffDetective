#!/usr/bin/env bash

resultsdir="$1"

cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1

java -cp "target/diffdetective-$(./scripts/version.sh)-jar-with-dependencies.jar" org.variantsync.diffdetective.tablegen.MiningResultAccumulator "$resultsdir" "$resultsdir" &&
echo "genUltimateResults.sh DONE"
