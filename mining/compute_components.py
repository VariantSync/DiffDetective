import os
import sys
import json
import networkx as nx
from math import ceil
from networkx.readwrite import json_graph
import numpy as np
import re
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
    filtered_total = 0
    if filtered:
        components, filtered_total = filter_too_large(*filter_too_many_similar_nodes(components, 0, filter_config.filter_too_many_similar_max_similar, filter_config.filter_too_many_similar_max_nodes), filter_config.filter_too_large_nb_nodes, filter_config.filter_too_large_nb_edges)
        
    print("We have %d connected components in %d graphs. From these components %d have beend filtered." % (len(components) + filtered_total, len(graphs), filtered_total))
        
    return components, nb_of_components_per_graph, filtered_total

# Filters components with more than nb_nodes/nb_edges nodes/edges. Use -1 for infinity.
def filter_too_large(components: list, filtered_total: int, nb_nodes=18, nb_edges=40):
    new_components = []

    for component in components:
        if not (nb_nodes != -1 and (component.number_of_nodes() > nb_nodes or component.number_of_edges() > nb_edges)):
            new_components.append(component)
    
    
    filtered_total += len(components)-len(new_components)
    print("Filtered out %d components that are too large, i.e., more than %d nodes or %d edges" % (filtered_total, nb_nodes, nb_edges))
    return new_components, filtered_total


# Several filters need to be applied to filter out components which could lead to too high computational efforts
def filter_too_many_similar_nodes(components: list, filtered_total: int, max_similar=2, max_nodes=10):
    new_components = []

    for component in components:
        labels = label_count_for_component(component)
        # if there are more than max_similar node labels with more than max_nodes
        if not (np.sum(np.array(list(labels.values())) > max_nodes) > max_similar):
            new_components.append(component)
    
    filtered_total += len(components)-len(new_components)
    print("Filtered out %d components with too many similar nodes, i.e., more than %d labels appeared more than %d times" % (filtered_total, max_similar, max_nodes))
    return new_components, filtered_total


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
        
    
    return components, nb_of_components_per_diff, filtered_total

    
def main(input_folder, output_folder, formatting=INPUT_FORMAT_NX, max_components_per_file=200):
    # TODO doing this with streams and yield would be a nicer solution to the chunking.
    components, nb_of_components_per_diff, filtered_total = get_components(input_folder, formatting=formatting, filtered=True)
    
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
        f.write(str(len(components)) + "," + str(filtered_total))

if __name__ == "__main__":
    if len(sys.argv) == 3:
        main(sys.argv[1], sys.argv[2])
    elif len(sys.argv) == 4:
        main(sys.argv[1], sys.argv[2], formatting = sys.argv[3])	
    else:
        print("Unexpected number of arguments. At least input path and output path has to be provided. Optionally as a third argument put NX or LG indicating the input graph formatting.")
