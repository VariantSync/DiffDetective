import os
import sys
import json
import networkx as nx
from math import ceil
from networkx.readwrite import json_graph
import numpy as np
import re
import time
from parse_utils import export_TLV, export_aids, export_subdue_c_graph, export_subdue_python_json, load_components_networkx, convert_node_link_graph_to_parsemis_directed_graph, convert_node_link_graph_to_subdue_c_graph, convert_node_link_graph_to_subdue_python_graph, import_tlv_folder, export_node_link_graph_from_subdue_c_graph, convert_node_link_graph_to_nx_graph

INPUT_FORMAT_NX = "NX"
INPUT_FORMAT_LG = "LG"

class FilterConfig():
    def __init__(self, filter_too_large_nb_nodes=15, filter_too_large_nb_edges=30, filter_too_many_similar_max_similar=2, filter_too_many_similar_max_nodes=10):
        self.filter_too_large_nb_nodes = filter_too_large_nb_nodes
        self.filter_too_large_nb_edges = filter_too_large_nb_edges
        self.filter_too_many_similar_max_similar = filter_too_many_similar_max_similar
        self.filter_too_many_similar_max_nodes = filter_too_many_similar_max_nodes

def connected_components(graph):
    components = [graph.subgraph(c).copy() for c in nx.weakly_connected_components(graph)]
    return components

def get_graph_components(graphs, filtered=False, filter_config=None):
    '''
    Each of the given graphs is devided into its connected components. These components are then optionally filtered according to the filter_config.
    
    :return: a tuble of a list of all remaining graph components, an array with the number of components per input graph, and the total number of removed graphs
    '''
    components = []
    nb_of_components_per_graph = []
    for graph in graphs:
        # Compute connected components for the diff graph
        new_components = connected_components(graph)

        nb_of_components_per_graph.append(len(new_components))
        components += new_components
    if filtered:
        
        components, filtered = filter_too_large(*filter_too_many_similar_nodes(components, {}, filter_config.filter_too_many_similar_max_similar, filter_config.filter_too_many_similar_max_nodes), filter_config.filter_too_large_nb_nodes, filter_config.filter_too_large_nb_edges)
        
    #print("We have %d connected components in %d graphs. From these components %d have beend filtered." % (len(components) + sum(filtered.values()), len(graphs), filtered_total))
        
    return components, nb_of_components_per_graph, filtered

# Filters components with more than nb_nodes/nb_edges nodes/edges. Use -1 for infinity.
def filter_too_large(components: list, filtered: dict, nb_nodes=18, nb_edges=40):
    new_components = []

    for component in components:
        if not (nb_nodes != -1 and (component.number_of_nodes() > nb_nodes or component.number_of_edges() > nb_edges)):
            new_components.append(component)
    
    
    filtered["too_large"] = len(components)-len(new_components)
    #print("Filtered out %d components that are too large, i.e., more than %d nodes or %d edges" % (filtered["too_large"], nb_nodes, nb_edges))
    return new_components, filtered


# Several filters need to be applied to filter out components which could lead to too high computational efforts
def filter_too_many_similar_nodes(components: list, filtered: dict, max_similar=2, max_nodes=10):
    new_components = []

    for component in components:
        labels = label_count_for_component(component)
        # if there are more than max_similar node labels with more than max_nodes
        if not (np.sum(np.array(list(labels.values())) > max_nodes) > max_similar):
            new_components.append(component)
    
    filtered["too_many_similar"] = len(components)-len(new_components)
    #print("Filtered out %d components with too many similar nodes, i.e., more than %d labels appeared more than %d times" % (filtered["too_many_similar"] , max_similar, max_nodes))
    return new_components, filtered


def label_count_for_component(component):
    labels = {}
    for node in component.nodes(data=True):
        if node[1]['label'] in labels.keys():
            labels[node[1]['label']] += 1
        else:
            labels[node[1]['label']] = 1
    return labels


def get_components(input_folder, formatting=INPUT_FORMAT_NX, filtered=False):
    # Load components
    if formatting == INPUT_FORMAT_NX:
        # TODO add support for filtering also for networkx input
        components, nb_of_components_per_diff = load_components_networkx(input_folder, filtered=filtered)
        filtered_total = 0
    if formatting == INPUT_FORMAT_LG:
        graphs = import_tlv_folder(input_folder, parse_support=False)
        components, nb_of_components_per_diff, filtered_total = get_graph_components(graphs, filtered=filtered, filter_config = FilterConfig())
        
    

    do_statistics(components)
    return components, nb_of_components_per_diff, filtered_total

def do_statistics(components): 
    have_specialization = [component for component in components if has_node(component, 'c4')]
    have_generalization =  [component for component in components if has_node(component, 'c5')]
    have_refactoring =  [component for component in components if has_node(component, 'c7')]
    have_reconfiguration =  [component for component in components if has_node(component, 'c6')]
    have_s_g = [component for component in have_specialization if component in have_generalization]
    have_s_ref = [component for component in have_specialization if component in have_refactoring]
    have_s_rec =  [component for component in have_specialization if component in have_reconfiguration]
    have_g_ref =  [component for component in have_generalization if component in have_refactoring]
    have_g_rec = [component for component in have_generalization if component in have_reconfiguration]
    have_ref_rec = [component for component in have_refactoring if component in have_reconfiguration]
    total =  len(components)
    print(f"total: {len(components)}; s: {len(have_specialization)/total}; g: {len(have_generalization)/total}; ref: {len(have_refactoring)/total}; rec: {len(have_reconfiguration)/total};")
    print(f"s_g: {len(have_s_g) / total}; s_ref: {len(have_s_ref) / total}; s_rec: {len(have_s_rec) / total}; g_ref: {len(have_g_ref) / total}; g_rec: {len(have_g_rec) / total}; ref_rec: {len(have_ref_rec) / total}")
    print(f"s_g: {len(have_specialization) * len(have_generalization) / (total**2)}; s_ref: {len(have_specialization) * len(have_refactoring) / (total**2)}; s_rec: {len(have_specialization) * len(have_reconfiguration) / (total**2)}; g_ref: {len(have_generalization) * len(have_refactoring) / (total ** 2)};  g_rec: {len(have_generalization) * len(have_reconfiguration) / (total**2)} ref_rec: {len(have_refactoring) * len(have_reconfiguration) / (total**2)};")

def has_node(graph, label):

    return label in [node[1]['label'] for node in list(graph.nodes(data=True))]
    
def main(input_folder, output_folder, dataset_name, max_components_per_file=200, formatting=INPUT_FORMAT_NX):
    # TODO doing this with streams and yield would be a nicer solution to the chunking.
    components, nb_of_components_per_diff, filtered = get_components(input_folder, formatting=formatting, filtered=True)
    for component in components:
        if not nx.is_directed_acyclic_graph(component):
            print(f"WARN: THERE ARE NON DAG GRAPHS IN THE INPUT: {component.name}")
   
    components_batched = [components[i*max_components_per_file:min((i+1)*max_components_per_file, len(components))] for i in range(ceil(len(components)/max_components_per_file))]
    
    # Create output folder if it doesn't exist yet
    os.makedirs(output_folder, exist_ok=True)

    # Exports
    for idx, batch in enumerate(components_batched):
        export_TLV(batch, output_folder + '/connected_components_' + str(idx) + '.lg')
        export_aids(batch, output_folder + '/connected_components_' + str(idx) + '.aids')
        # Write length in extra file
        with open(output_folder + '/connected_components_' + str(idx) + '.count', 'w') as f:
            f.write(str(len(batch)))
        #export_subdue_c_graph(batch, set_name + '/connected_components.g')
        #export_subdue_python_json(batch, set_name + '/connected_components.json')

    with open(output_folder + '/filter_stats.csv', 'w') as f:
        # ds name, all components, components after filtering, filtered too large, filtered too many similar
        
        f.write(dataset_name + "," + str(len(components) + filtered["too_large"] + filtered["too_many_similar"]) + "," + str(len(components)) + "," + str(filtered["too_large"])+ "," + str(filtered["too_many_similar"]))

if __name__ == "__main__":
    start_time = time.time()
    if len(sys.argv) == 5:
        main(sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4]))
    elif len(sys.argv) == 6:
        main(sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4]), formatting = sys.argv[5])	
    else:
        print("Unexpected number of arguments. At least input path, output path, dataset name, as well as batch_size has to be provided. Optionally as a fourth argument put NX or LG indicating the input graph formatting.")
    end_time = time.time()
    with open(sys.argv[2] + 'time.txt', 'w') as f:
        f.write(str(end_time-start_time))
