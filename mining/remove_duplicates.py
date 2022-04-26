from isograph import remove_duplicates
from parse_utils import import_tlv_folder, export_TLV
import sys
import os
import time

def main(subgraphs_path, results_dir, dataset, output_single=False):
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
    if output_single:
        for i in range(len(subgraphs)):
            export_TLV([subgraphs[i]], results_dir + '/' + str(i) + '.lg')
    else:
        export_TLV(subgraphs, results_dir + '/results_no_duplicates.lg')
    
    with open(results_dir + '/results.csv', 'w') as f:
        f.write(dataset + "," + str(nb_initial_subgraphs) +"," + str(nb_pruned_subgraphs) + "," + str(removed_duplicates))
        
if __name__ == "__main__":
    if len(sys.argv) < 4 or len(sys.argv) > 5:
        print("Three or four arguments expected: path to input graph database, path to output results, and optionally if every subgraph should be written to a single file. Run as python remove_duplicates.py [input_folder] [output_folder] [dataset_name] [True/False]")
    
    # Create output folder if it doesn't exist yet
    os.makedirs(sys.argv[2], exist_ok=True)
 
    start_time = time.time()
    if len(sys.argv) == 4:
        main(sys.argv[1], sys.argv[2], sys.argv[3])
    if len(sys.argv) == 5:
        main(sys.argv[1], sys.argv[2], sys.argv[3], bool(sys.argv[4]))
    end_time = time.time()
    with open(sys.argv[2] + 'time.txt', 'w') as f:
        f.write(str(end_time-start_time))
