from isograph import remove_duplicates
from parse_utils import import_tlv_folder, export_TLV
import sys
import os

def main(subgraphs_path, results_dir, dataset):
    # Import graphs
    print("Parsing graph input files")
    subgraphs = import_tlv_folder(subgraphs_path, parse_support=False)
    # Get rid of clones
    nb_initial_subgraphs = len(subgraphs)
    print("Removing duplicates. This might take some time...")
    subgraphs = remove_duplicates(subgraphs)
    nb_pruned_subgraphs = len(subgraphs)
    removed_duplicates = nb_initial_subgraphs - nb_pruned_subgraphs
    print("Removed %d duplicates" % removed_duplicates)
    
    # Load graphs
    
    
    print("Writing subgraphs.")
    export_TLV(subgraphs, results_dir + './results_no_duplicates.lg')
    with open(results_dir + './results.csv', 'w') as f:
        f.write(dataset + "," + str(nb_initial_subgraphs) +"," + str(nb_pruned_subgraphs) + "," + str(removed_duplicates))
        
if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Three arguments expected: path to input graph database, path to output results. Run as python remove_duplicates.py [input_folder] [output_folder] [dataset_name]")
    
    # Create output folder if it doesn't exist yet
    os.makedirs(sys.argv[2], exist_ok=True)
    
    main(sys.argv[1], sys.argv[2], sys.argv[3])
