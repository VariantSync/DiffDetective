resultsdir=$1

java -cp "target/diffdetective-$(./scripts/version.sh)-jar-with-dependencies.jar" org.variantsync.diffdetective.tablegen.MiningResultAccumulator $resultsdir $resultsdir
echo "genUltimateResults.sh DONE"
