#!/bin/bash

# call as pipline.sh [input-path] [output-path] [lib-path]
# e.g. ./pipeline.sh '/home/schwinnez/projects/Frequent Subgraph Mining/case studies/variability_patterns/dataset_1/diffgraphs/' '/home/schwinnez/projects/Frequent Subgraph Mining/case studies/variability_patterns/dataset_1/output/' './lib/'





python_version=$(python3 --version)
echo "Using python version $python_version"
# TODO automatically make sure that python 3 is used

target_subtree_count_for_threshold_estimation=300
threshold=10 # 0 means read threshold from files
batch_size=1000
timeout_mining=120
min_size=5
max_size=15


echo "Input:  $1"
echo "Output:  $2"
echo "Libs:  $3"

mkdir -p "$2"


run_dataset(){
	echo "Input:  $1"
	echo "Output:  $2"
	echo "Libs:  $3"

	# Definition of output directories
	data_set_name="$4"
	input_path="$1"
	output_filtered="$2filtered/"
	output_mining="$2mining/"
	output_mining_no_duplicates="$2mining_no_duplicates/"
	lib_path="$3"
	parsemis_path="$3parsemis.jar"

	echo "Running dataset: $data_set_name"
	mkdir -p "$2"


	# Step 1 - Read graph databases, filter and chunk - Default filter config: Not larger than 15 nodes, 30 edges, no more than
	python3 compute_components.py "$input_path" "$output_filtered" "$data_set_name" $batch_size LG

	# Step 2 - Compute thresholds - Not better than fixed threshold for Linux dataset
	#python bisect_threshold_search.py $lib_path $output_filtered $target_subtree_count_for_threshold_estimation

	# Step 3 - Mining
	python3 run_parsemis.py "$parsemis_path" "$output_filtered" "$output_mining" $threshold $min_size $max_size $timeout_mining

	# Step 4 - Remove duplicates
	python3 remove_duplicates.py "$output_mining" "$output_mining_no_duplicates" "$data_set_name" 
}

# Run for every dataset in input folder
for input_folder in "$1"/*/ ; do
	dataset="$(basename "$input_folder")"
	output_base="$2/$batch_size-$threshold/$dataset/"
	run_dataset "$input_folder" "$output_base" "$3" "$dataset"
done



