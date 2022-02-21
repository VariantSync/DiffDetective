'''
The idea of this module is, to find a good threshold for exact frequent subgraph mining based on the results of an approximate tree mining result.
This is kind of a measure for the difficulty of the dataset. The concrete numbers have to be determined manually, based on available hardware resources and gut feeling.
'''
from math import ceil
import subprocess
import os
import sys
import re

regex_count_file = r".*_(\d+).*"

def bisect_threshold(lib_path, data_path, target_count=500):
    ''' Uses a bisection method to find a threshold which closest comes to target_count output graphs with an approximate miner.
    The input directory should contain the graph database as .lg file, the number of graphs in the file as .count file.
    It is assumed, that the count files are enumerated the same way as the graph databases, so that a lexicographic sorting gives matching files.'''
    target_count = int(target_count)
    
    # First check, that we have as many count-files as graph files.
    db_files = sorted([file_name for file_name in os.listdir(data_path) if file_name.endswith('.aids')])
    count_files = sorted([file_name for file_name in os.listdir(data_path) if file_name.endswith('.count')])
    assert len(db_files) == len(count_files)
    
    
    for idx, in_file in enumerate(db_files):
        match_filename = re.match(regex_count_file, in_file)
        
        if not match_filename:
            assert False, "Filename for db_size not formatted as expected."
        
        
        database_id = int(match_filename.group(1))
        
        
        t_max = get_db_size(data_path + count_files[idx])
        t_min = 2
        
        # Bisection to find a heuristically good threshold
        while t_max - t_min > 1:
            t = (t_max + t_min) // 2
            found_patterns = run_approximate(lib_path, data_path + in_file, data_path + 'frequent_temp.cstring', t)
            
            if found_patterns >= target_count:
                t_min = t	
            else:
                t_max = t
                

        # Write found threshold to file
        with open(data_path +  str(database_id) + '.threshold', 'w') as f:
            f.write(str(t))   
       
    
def get_db_size(file_name):
    with open(file_name) as f:
        return int(f.read())
    
def count_subgraphs(subgraph_file):
    ''' Assume a AIDS format. In this format, every graph is represented by 3 lines.'''
    i = None
    with open(subgraph_file) as f:
        for i, l in enumerate(f):
            pass
    if i is not None:
        return ceil((i + 1) / 3)
    else:
        return 0
    
def run_approximate(lib_path, input_file, output_file, threshold):
    ''' We fix the maximum length to l, i.e., we are mining patterns at most l nodes large. '''
    
    # Run HOPS approximate subgraph miner
    lwg_cmd_template = "'{lib_path}lwg' -t {threshold} -p 8 -e hops -i 5 '{input_file}' -o '{output_file}'"
    miner_cmd = lwg_cmd_template.format(lib_path=lib_path, input_file=input_file, output_file=output_file, threshold=threshold)
        
    p = subprocess.Popen(miner_cmd, shell=True)
       
    try:
        p.wait(30)  # Should take at most 30 seconds
    except Exception as e:
        print(str(e))
        p.kill()    
        
    # transform output file (so that we can easier count the number of patterns later
    cstring_cmd = "cat " + output_file + " | xargs -I {} bash -c \"echo '{}' | '" + lib_path + "cstring' -i -\"  > " + output_file + ".tmp"
    
    subprocess.run(cstring_cmd, shell = True)

    #os.remove(output_file)
    nb_subgraphs = count_subgraphs(output_file+".tmp")
    #os.remove(output_file+".tmp")
    return nb_subgraphs
    
if __name__ == "__main__":
    if len(sys.argv) == 4:
        bisect_threshold(sys.argv[1], sys.argv[2], target_count = sys.argv[3])	
    else:
        print("Unexpected number of arguments. Run as python bisect_threshold_search.py [lib_path] [data_path] [target_count]. lwg and cstring tool binaries need to be located in the lib_path. Data directiory is expected to contain .lg line graph databases and .count files with the number of graphs in the corresponding database.")
