import sys
import os
import subprocess
import signal
import re


regex_file_id = r".*_(\d+).*"

########################### Parsemis #####################################################
def run_parsemis(lib_path, in_folder, output_folder, threshold, min_size, max_size, memory='28G', nb_threads=12, timeout_seconds=180):

    # Template for shell command shell command
    parsemis_cmd_template = "java -Xmx{memory} -jar  '{parsemis_path}' --graphFile='{in_file}' --outputFile='{out_file}' --minimumFrequency={threshold} --maximumNodeCount={max_size} --minimumNodeCount={min_size} --algorithm=dagma --singleRooted=true --closeGraph=true --zaretsky=true --subdue=true --storeEmbeddings=true --distribution=threads --threads={nb_threads} --swapFile={swap_file}" 
    
    # Currently only support for line graph
    for idx, in_file in enumerate([file_name for file_name in os.listdir(in_folder) if file_name.endswith('.lg')]):
        match_id = re.match(regex_file_id, in_file)
        
        if not match_id:
            assert False, "Filename for not formatted as expected."
        
        
        database_id = int(match_id.group(1))
        
        # Read threshold from file (if no threshold is given)
        if threshold == 0:
            with open(in_folder + str(database_id) + ".threshold", 'r') as f:
                threshold = int(f.read())
            
        parsemis_cmd = parsemis_cmd_template.format(parsemis_path=lib_path, in_file=in_folder + in_file, out_file=output_folder+'/results_' + str(database_id) + '.lg', swap_file=output_folder+'\swap.tmp', threshold=threshold, max_size=max_size, min_size=min_size, memory=memory, nb_threads=nb_threads )
        # Run command (REQUIRES JAVA 8!!!)
        
        try:
            p = subprocess.Popen(parsemis_cmd, shell=True, start_new_session=True)
            error_code = p.wait(timeout=timeout_seconds)
        except subprocess.TimeoutExpired as to:
            print('Timeout.Terminating the whole process group...', file=sys.stderr)

            os.killpg(os.getpgid(p.pid), signal.SIGKILL)
            with open(output_folder+'/results_'+ str(database_id) + '.err', 'w') as f:
                f.write(str(to))
            
            continue
        
        if error_code != 0:
            print('Error. Got code:' + str(error_code))
            with open(output_folder+'/results_'+ str(database_id) + '.err', 'w') as f:
                f.write(str(error_code))

########################## End Run Parsemis ##############################################


if __name__ == "__main__":
    if len(sys.argv) < 7:
        print("Call like python run_parsemis.py [lib_path] [in_folder] [out_folder] [threshold] [min_size] [max_size]")
        
    # Create output folder if it doesn't exist yet
    os.makedirs(sys.argv[3], exist_ok=True)
    
    
    run_parsemis(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], sys.argv[6])
