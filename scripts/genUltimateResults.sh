resultsdir=$1

java -cp "target/diffdetective-1.0.0-jar-with-dependencies.jar" org.variantsync.diffdetective.tablegen.MiningResultAccumulator $resultsdir $resultsdir
echo "genUltimateResults.sh DONE"
