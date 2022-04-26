input_base=$1
output_base=$2

mkdir -p "$output_base"
mkdir -p "$output_base/temp/"

# Run for every dataset in input folder
for dataset in "$input_base"/*/ ; do
    dataset_name="$(basename "$dataset")"
    subgraphs_file="$dataset/mining_no_duplicates/results_no_duplicates.lg" 
    if [ -e $subgraphs_file ]
      then cp "$subgraphs_file" "$output_base/temp/$dataset_name.lg"
    fi
done

# Remove duplicates and output every subgraph as single file
python3 remove_duplicates.py "$output_base/temp/" "$output_base" "all subgraphs" "True"
rm -rf  "$output_base/temp/"
