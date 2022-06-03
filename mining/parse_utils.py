import os
import sys
import json
import networkx as nx
from networkx.readwrite import json_graph
import numpy as np
import re

# See https://github.com/pwelke/hops/blob/master/smallgraphs/util/converter/gSpan2aids.py
def export_aids(graph_db, path):
    f = open(path, 'w')
    i = 0
    for graph in graph_db:
        temp_graph = nx.convert_node_labels_to_integers(graph, first_label=0)
        vertices = temp_graph.nodes(data=True)
        edges = temp_graph.edges(data=True)
        if len(vertices) == 0:
            return
        # write graph header
        f.write('# ' + str(i) + ' 0 ' + str(len(vertices)) + ' ' + str(len(edges)) + '\n')
        # TODO: sort and check if vertex indices are correctly ordered
        for node, data in vertices:
            f.write(data['label'] + ' ')
        f.write('\n')
        for source, target, data in edges:
            f.write(str(int(source) + 1) + ' ' + str(int(target) + 1) + ' ' + data['label'] + ' ')
        f.write('\n')
        i = i + 1
    f.close()


def export_subdue_c_graph(graph_db, path):
    with open(path, 'w') as output_graph_file:
        last_node_id_from_last_graph = 1
        last_edge_id_from_last_graph = 1

        for i, graph in enumerate(graph_db):
            temp_graph = nx.convert_node_labels_to_integers(graph, first_label=0)
            vertices = temp_graph.nodes(data=True)
            edges = temp_graph.edges(data=True)

            starting_node_id_from_current_graph = last_node_id_from_last_graph

            for n, node in enumerate(vertices):
                output_graph_file.write('v ' + str(last_node_id_from_last_graph + n) + ' ' + node[1]['label'] + '\n')
                # Last edge
                if n == len(vertices) - 1:
                    last_node_id_from_last_graph = last_node_id_from_last_graph + n + 1

            for j, edge in enumerate(edges):
                output_graph_file.write('e ' + str(edge[0] + starting_node_id_from_current_graph) + ' ' + str(
                    edge[1] + starting_node_id_from_current_graph) + ' ' + str(edge[2]['label']) + '\n')


def export_subdue_python_json(graph_db, path):
    with open(path, 'w') as output_graph_file:
        output_graph_file.write('[\n')
        last_node_id_from_last_graph = 1
        last_edge_id_from_last_graph = 1

        for i, graph in enumerate(graph_db):
            temp_graph = nx.convert_node_labels_to_integers(graph, first_label=0)
            vertices = temp_graph.nodes(data=True)
            edges = temp_graph.edges(data=True)

            starting_node_id_from_current_graph = last_node_id_from_last_graph

            for n, node in enumerate(vertices):
                output_graph_file.write('  {"vertex": {\n')
                output_graph_file.write('    "id": "' + str(last_node_id_from_last_graph + n) + '",\n')
                output_graph_file.write('    "attributes": {"label": "' + node[1]['label'] + '"}}},\n')
                # Last edge
                if n == len(vertices) - 1:
                    last_node_id_from_last_graph = last_node_id_from_last_graph + n + 1

            for j, edge in enumerate(edges):
                output_graph_file.write('  {"edge": {\n')
                output_graph_file.write('    "id": "' + str(last_edge_id_from_last_graph + j) + '",\n')
                output_graph_file.write('    "source": "' + str(edge[0] + starting_node_id_from_current_graph) + '",\n')
                output_graph_file.write('    "target": "' + str(edge[1] + starting_node_id_from_current_graph) + '",\n')
                output_graph_file.write('    "directed": "false",\n')

                # Check for the last edge
                if j == len(edges) - 1:
                    last_edge_id_from_last_graph = last_edge_id_from_last_graph + j + 1
                    # Dont write a comma for the last attribute in the file
                    if len(graph_db) == i + 1:
                        output_graph_file.write('    "attributes": {"label": "' + str(edge[2]['label']) + '"}}}\n')
                    else:
                        output_graph_file.write('    "attributes": {"label": "' + str(edge[2]['label']) + '"}}},\n')
                else:
                    output_graph_file.write('    "attributes": {"label": "' + str(edge[2]['label']) + '"}}},\n')

        output_graph_file.write(']\n')


# TODO use get_graph_components here
# TODO this can be done asynchronously using yield
def load_components_networkx(folder, filtered=False):
    components = []
    nb_of_components_per_diff = []
    for filename in os.listdir(folder):
        if not filename.endswith('.json'):
            continue
        with open(os.path.join(folder, filename), 'r') as f:  # open in readonly mode
            json_str = f.read()
            data = json.loads(json_str)
            f.close()
            H = json_graph.node_link_graph(data)
            # Compute connected components for the diff graph
            new_components = connected_components(H)
            if filtered:
                new_components = filter_too_large(filter_too_many_similar_nodes(new_components))
            nb_of_components_per_diff.append(len(new_components))
            components += new_components
    return components, nb_of_components_per_diff


def convert_node_link_graph_to_parsemis_directed_graph(input_file, output_file):
    """
    Convert node link graph to graph used in the parsemis graph lib https://github.com/timtadh/parsemis.
    """
    json_file = json.load(open(input_file))

    with open(output_file, 'a') as the_file:
        the_file.write('t # graph_id\n')

        # Add vertex
        for node in json_file['nodes']:
            the_file.write('v' + ' ' + str(node['id']) + ' ' + node['label'] + '\n')

        # Add edge
        for i, edge in enumerate(json_file['links']):
            the_file.write('e' + ' ' + str(edge['source']) + ' ' + str(edge['target']) + ' ' + edge['label'] + '\n')


def convert_node_link_graph_to_subdue_c_graph(input_file, output_file):
    """
    Convert node link graph to graph used in the subdue_python c implementation https://github.com/gromgull/subdue.
    """
    json_file = json.load(open(input_file))

    with open(output_file, 'a') as the_file:

        # Add vertex
        for node in json_file['nodes']:
            the_file.write('v' + ' ' + str(int(node['id']) + 1) + ' ' + node['label'] + '\n')

        # Add edge
        for i, edge in enumerate(json_file['links']):
            the_file.write('u' + ' ' + str(int(edge['source']) + 1) + ' ' +
                           str(int(edge['target']) + 1) + ' ' + edge['label'] + '\n')


def convert_node_link_graph_to_subdue_python_graph(input_file, output_file):
    """
    Convert node link graph to graph used in the subdue_python python implementation https://github.com/holderlb/Subdue.
    """
    json_file = json.load(open(input_file))

    with open(output_file, 'a') as the_file:
        the_file.write('[\n')

        # Add vertex
        for node in json_file['nodes']:
            the_file.write('  {"vertex": {\n')
            the_file.write('    "id": "' + str(node['id']) + '",\n')
            the_file.write('    "attributes": {"label": "' + node['label'] + '"}}},\n')

        # Add edge
        for i, edge in enumerate(json_file['links']):
            the_file.write('  {"edge": {\n')
            the_file.write('    "id": "' + str(i + 1) + '",\n')
            the_file.write('    "source": "' + str(edge['source']) + '",\n')
            the_file.write('    "target": "' + str(edge['target']) + '",\n')
            the_file.write('    "directed": "false",\n')
            if i != len(json_file['links']) - 1:
                the_file.write('    "attributes": {"label": "' + edge['label'] + '"}}},\n')
            # Last element
            else:
                the_file.write('    "attributes": {"label": "' + edge['label'] + '"}}}\n')

        # Add edge
        the_file.write(']\n')
        
        
########################### Line Graph Parsers/Serializers #####################################################
# Export TLV (gSpan consumes TLV)
def export_TLV(graph_db, path):
    f = open(path, 'w')
    for graph in graph_db:
        f.write('t # ' + graph.name + '\n')
        temp_graph = nx.convert_node_labels_to_integers(graph, first_label=0)
        # sort indices
        vertices = temp_graph.nodes(data=True)
        for node, data in vertices:
            if 'label' not in data.keys():
                print("WARN: Unlabeled nodes in graph data for graph %s." % graph.name)
                label = "UNKNOWN_LABEL"
            else:
                label = data['label']
                
            f.write("v " + str(node) + " " + label + '\n')
        edges = temp_graph.edges(data=True)
        for source, target, data in edges:
            if 'label' not in data.keys():
                print("WARN: Unlabeled edges in graph data for graph %s." % graph.name)
                label = "UNKNOWN_LABEL"
            else:
                label = data['label']
            f.write("e " + str(source) + " " + str(target) + " " + label + '\n')
    f.close()

def import_tlv_folder(folder_path, postfix='.lg', parse_support=True):
    '''
    See import_tlv. Just iterates over the folder and concats. 
    Doesn't take graph isomorphisms into account, i.e., isomorphic graphs in different files could appear as duplicates.
    '''
    if parse_support:
        graphs = {}
    else:
        graphs = []
        
    for file in os.listdir(folder_path):
        if file.endswith(postfix):
            new_graphs = import_tlv(os.path.join(folder_path, file), parse_support)
            if parse_support:
                graphs.update(new_graphs)
            else:
                graphs += new_graphs
    return graphs

def import_tlv(path, parse_support=True):
    '''
    Parses the given file as line graph and (optionally) parses the support of the graph. There are multiple different formats to obtain the support from.
    It returns a dictionary of graphs with their support or a list of all graphs if no support parsing is desired.
    
    params: path, the path to the line graph file
            parse_support: true, if support should also be parsed
    returns:  a dictionary dict(DiGraph, int) with the graphs and their suppor, if parse_support=True, else a list of graphs
    '''
    graph_db = open(path, 'r')
    next_line = graph_db.readline()
    if parse_support:
        graphs = {}
    else:
        graphs = []
    regex_header = r"t # (.*)"
    regex_node = r"v (\d+) (.+).*"
    regex_edge = r"e (\d+) (\d+) (.+).*"
    
    # Some file formats give the support directly, others list all the embeddings. We support both options.
    regex_support = r"Support: (\d+).*"
    regex_embedding = r"#=> ([^\s]+) .*"
    
    # if tlv header continue parsing
    match_header = re.match(regex_header, next_line)
    if match_header:
        next_line = graph_db.readline()
    else:
        print("Error parsing graph db. Expecting TLV.")
        return {}

    while next_line:
        graph = nx.DiGraph()
        graph.name = match_header.group(1)
        support_set = set()
        support = None
        match_header = None

        
        while next_line and not match_header:
            match_node = re.match(regex_node, next_line)
            match_edge = re.match(regex_edge, next_line)
            match_support= re.match(regex_support, next_line)
            match_embedding= re.match(regex_embedding, next_line)
            if match_node:
                graph.add_node(int(match_node.group(1)), label=str(match_node.group(2)))
            elif match_edge:
                graph.add_edge(int(match_edge.group(1)), int(match_edge.group(2)), label=str(match_edge.group(3)))
            elif match_support:
                support = int(match_support.group(1))
            elif match_embedding:
                support_set.add(str(match_embedding.group(1)))
            next_line = graph_db.readline()
            if next_line:
                match_header = re.match(regex_header, next_line)
        
        if support_set is not None:
            graph.graph['embeddings'] = str(support_set)

        if (support is None and support_set == set() and parse_support):
            print("WARN: Error parsing line graph with graph support. Check format.")
        elif not parse_support:
            graphs.append(graph)
        elif support is not None:
            graphs[graph] = support
        else:
            support = len(support_set)
            graphs[graph] = support
        next_line = graph_db.readline()
    return graphs

########################### End: Line Graph Parsers #####################################################

def export_node_link_graph_from_subdue_c_graph(input_file, output_file):
    empty_input = False
    with open(output_file, 'w') as output:
        output.write("[\n")

        with open(input_file, "r") as check_empty:
            if check_empty.readline() == "":
                empty_input = True

        if empty_input is False:
            with open(input_file, "r") as input_g:
                edge_id = 1
                for line in input_g.readlines():
                    if line.startswith("v"):
                        elements = line.split(" ")
                        label_without_linebreak = elements[2].split("\n")
                        output.write('  {"vertex": {\n')
                        output.write('    "id": "' + elements[1] + '",\n')
                        output.write('    "attributes": {"label": "' + label_without_linebreak[0] + '"}}},\n')
                    if line.startswith("u"):
                        elements = line.split(" ")
                        label_without_linebreak = elements[3].split("\n")
                        output.write('  {"edge": {\n')
                        output.write('    "id": "' + str(edge_id) + '",\n')
                        output.write('    "source": "' + elements[1] + '",\n')
                        output.write('    "target": "' + elements[2] + '",\n')
                        output.write('    "directed": "false",\n')
                        output.write('    "attributes": {"label": "' + label_without_linebreak[0] + '"}}},\n')
                        edge_id = edge_id + 1
        output.write("]")

    if empty_input is False:
        with open(output_file, 'rb+') as remove_last_comma_handler:
            remove_last_comma_handler.seek(-4, 2)
            remove_last_comma_handler.truncate()
        with open(output_file, "a") as add_last_bracket_handler:
            add_last_bracket_handler.write("\n]")


def convert_node_link_graph_to_nx_graph(file):
    json_file = json.load(open(file))
    nodes = []
    node_names = []
    edges = []
    edge_names = []

    for json_dict in json_file:
        for key, value in json_dict.items():
            if key == 'vertex':
                nodes.insert(int(value['id']), value['attributes']['label'])
                node_names.insert(int(value['id']), value['id'])

            if key == 'edge':
                edges.insert(int(value['id']), [value['source'], value['target']])
                edge_names.insert(int(value['id']), value['attributes']['label'])

    graph = nx.DiGraph()

    # Add nodes to nx graph
    i = 0
    for node in node_names:
        graph.add_node(node, label=nodes[i])
        i = i + 1

    # Add edges to nx graph
    i = 0
    for edge in edges:
        graph.add_edge(edge[0], edge[1], label=edge_names[i])
        i = i + 1

    return graph
