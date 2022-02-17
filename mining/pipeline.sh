#!/bin/bash


# Definition of output directories
output_filtered="$2filtered/"
output_mining="$2mining/"
output_mining_no_duplicates="$2mining_no_duplicates/"
lib_path="$3"
parsemis_path="$3parsemis.jar"
target_subtree_count_for_threshold_estimation=300
threshold=10 # 0 means read threshold from files
min_size=4
max_size=15

# Step 1 - Read graph databases, filter and chunk - Default filter config: Not larger than 15 nodes, 30 edges, no more than
python compute_components.py $1 $output_filtered LG

# Step 2 - Compute thresholds - Not better than fixed threshold for Linux dataset
#python bisect_threshold_search.py $lib_path $output_filtered $target_subtree_count_for_threshold_estimation

# Step 3 - Mining
python run_parsemis.py $parsemis_path $output_filtered $output_mining $threshold $min_size $max_size

# Step 4 - Remove duplicates
python remove_duplicates.py output_mining output_mining_no_duplicates
