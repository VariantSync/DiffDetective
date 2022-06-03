input_folder=$1
output_folder=$2

java -cp "target/diffdetective-1.0.0-jar-with-dependencies.jar" org.variantsync.diffdetective.mining.postprocessing.MiningPostprocessing $input_folder $output_folder
echo "runPostprocessing.sh DONE"
