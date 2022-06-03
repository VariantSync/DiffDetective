###############################################
###############################################
#### Module: Remove duplicates ################
### and compute counting statistics ###########
###############################################
###############################################
import networkx as nx
from typing import List
from networkx.readwrite.graphml import generate_graphml
from parse_utils import import_tlv_folder, export_TLV
from plot_utils import plot_graphs, plot_graph_dot
from isograph import IsoGraph, remove_duplicates
import csv
import sys
import os
import time


class LatticeNode():
    '''Wraps IsoGraphs in a data structure for a lattice, i.e., links every graph to it's parents or childrens, respectively.
    Furthermore, the lattice nodes have pointers to graphs in a graph_database where they are contained in (i.e., their occurrences).
    '''
    
    def __init__(self, graph: IsoGraph, label_name = 'label'):
        self.graph = graph
        self.graph.set_label(label_name)
        
        self.occurrences = []
        self.parents = []
        self.children = []
        # dictionary with keys = nodes and values = maximal number of steps
        self.reachable_nodes = dict()
        # Tracking this speeds-up the lattice generation
        self.no_parent_candidates = set()
        self.discovered = False
        # The layer is the longest path this node can be reached from the leaf nodes of a lattice
        self.layer = 0

    def __str__(self):
        return str(self.graph.name)
        
    __repr__ = __str__
            

class Lattice():
    def __init__(self, nodes: List[LatticeNode]):
        self.nodes = nodes
        self._compute_lattice_dfs()
        # There is a partial order on the lattice therefore we can define layers based on this partial order
        self.layers = dict()
        self._compute_layers()

    # TODO this lattice generation is very complex -> Try to simplify and reduce cognitive overhead. 
    # TODO e.g., bidirectional parent-child relationship could be handled automatically,... There is also a huge duplication related to this.
    def _compute_lattice_dfs(self, child=None, current_path=[]):
        ''' 
        Efficient method to compute the subgraph lattice. Note that this method does not yet yield the right layer for every lattice node.
        To compute the layers, run _compute_layers.
        '''

        if child is None:
            for node in self.nodes:
                node.no_parent_candidates.add(node)
                reachable_nodes = self._compute_lattice_dfs(child=node, current_path = [node])
            return
        
        if child.discovered:
            return
            
        for node in list(set(self.nodes)-child.no_parent_candidates):
            if not node.discovered :
                if node.graph.contains(child.graph):
                    # Propagate no_parent information futher up
                    node.no_parent_candidates = node.no_parent_candidates.union(child.no_parent_candidates)
                    node.no_parent_candidates.add(node) # "one can not reach oneself"
                    # Handle parent-child relationship
                    node.children.append(child)
                    child.parents.append(node)
                    # Proceed recursively
                    reachable_nodes = self._compute_lattice_dfs(node, current_path + [node])
                    # propagate down reachable nodes discovered recursively
                    for depth in range(len(current_path)):
                        length_to_node = (len(current_path) - depth)
                        current_path[depth].reachable_nodes[node] = length_to_node
                        for upper_parent in reachable_nodes.keys():
                            if upper_parent in current_path[depth].reachable_nodes.keys():
                                current_path[depth].reachable_nodes[upper_parent] = max(length_to_node + node.reachable_nodes[upper_parent], current_path[depth].reachable_nodes[upper_parent])
                            else:
                                current_path[depth].reachable_nodes[upper_parent] = length_to_node + node.reachable_nodes[upper_parent]                                 
                else:
                    # tracking this helps us speed up the lattice construction, since we can save some expensive subgraph monomorphism checks
                    child.no_parent_candidates.add(node)   
            else: 
                # check if we've discovered a longer path already (we want no shortcuts, always the full hiearchy in our child-pattern relationships)
                if not node in child.reachable_nodes.keys():
                    if node.graph.contains(child.graph):
                        # Propagate down reachable nodes for path and cut of short-cuts that have been just created
                        for depth in range(len(current_path)):
                            # Cut short-cuts, if we've just created a longer path
                            #if node in current_path[depth].parents:
                            #    current_path[depth].parents.remove(node)
                            #    node.children.remove(current_path[depth])
                            # Propagate down reachable nodes
                            length_to_node = (len(current_path) - depth)
                            current_path[depth].reachable_nodes[node] = length_to_node
                            for upper_parent in node.reachable_nodes.keys():
                                if upper_parent in current_path[depth].reachable_nodes.keys():
                                    current_path[depth].reachable_nodes[upper_parent] = max(length_to_node + node.reachable_nodes[upper_parent], current_path[depth].reachable_nodes[upper_parent])
                                else:
                                    current_path[depth].reachable_nodes[upper_parent] = length_to_node + node.reachable_nodes[upper_parent]

                        # Handle parent-child relationship
                        node.children.append(child)
                        child.parents.append(node)
        
        # At last, we need to handle a situation in which short-cuts might still be present. This can happen when they have been created bevor the other "paths" have been completly discoverd.
        # We can see the shortcuts now, because a shortcut means that a parent's reachability (which is the longest path to the node) is > 1
        parents_to_be_removed = []
        for parent in child.parents:
            if child.reachable_nodes[parent] > 1:
                # Have to remember what we want to delete, because we can not remove while iterating over a list (mixing up iterator in python)
                parents_to_be_removed.append(parent)
                parent.children.remove(child)  
        for parent in parents_to_be_removed:
            child.parents.remove(parent)
        
        # Node has finally been fully discovered
        child.discovered = True
        return child.reachable_nodes
        
    def _compute_layers(self):
        # Compute the max layer for each node (we need the maximum for bottom-up algos, to be sure in every new layer to know already all the children)
        for node in self.nodes:
            for reachable_node in node.reachable_nodes.keys():
                reachable_node.layer = max(reachable_node.layer, node.reachable_nodes[reachable_node])
        # Write the nodes know in the layer dict for easy layer access
        for node in self.nodes:
            node_layer = node.layer
            if node_layer not in self.layers.keys():
                self.layers[node_layer] = [node]
            else:
                self.layers[node_layer].append(node)
    
    
    def to_graphml(self):
        nx_lattice = self.to_networkx(include_graphs = False)
        return "\n".join(generate_graphml(nx_lattice))
    
    def to_networkx(self, include_graphs=True):
        '''Represents the lattice as a networkX graph'''
        nx_lattice = nx.DiGraph()
        
        # TODO deferring id resolution could speed up this graph generation (but should be fine with the lattice size we will face in practise)
        for i in range(len(self.nodes)):
            node = self.nodes[i]
            if include_graphs:
                nx_lattice.add_node(i, label = node.graph.name, graph = node.graph)
            else:
                nx_lattice.add_node(i, label = node.graph.name)
                
        for node in self.nodes:
            for child in node.children:
                nx_lattice.add_edge(self.nodes.index(node), self.nodes.index(child), label="subgraph")
                
        return nx_lattice
 
        
    
    # TODO only very simple printing of lattice. Proper plotting should be done in the future for debug purposes
    def describe(self, verbose=False):
        lines = []
        for layer in sorted(self.layers.keys()):
            lines.append("Layer:\t %d; Nodes: \t %s" % (layer, "|".join([node.graph.name for node in self.layers[layer]])))
            
        if verbose:
            lines += self._describe_verbose()
            
        return "\n".join(lines)
        
        
    def _describe_verbose(self):
        lines = []
        for node in self.nodes:
            lines.append("Node:\t %s; Children: \t %s" %(node.graph.name, "|".join([child.graph.name for child in node.children])))
            lines.append("Layer: %d; Reachable: %s" %(node.layer, node.reachable_nodes))
        return lines
    
    def count_occurrences(self, graph_database: List[IsoGraph]):
        for layer in sorted(self.layers.keys()):
            for node in self.layers[layer]:
                if layer != 0:
                    occurrence_candidates = list(set.intersection(*[set(child.occurrences) for child in node.children]))
                else:
                    occurrence_candidates = range(len(graph_database))
                    
                for occurrence_candidate in occurrence_candidates:
                    if graph_database[occurrence_candidate].contains(node.graph):
                        print(graph_database[occurrence_candidate].graph['embeddings'])
                        node.occurrences.append(occurrence_candidate)
      
    
class Statistics():
    '''
    Provides basic counting statistics for a set of subgraphs and a graph database.
    '''
    def __init__(self, graph_db: List[nx.DiGraph], subgraphs: List[nx.DiGraph], label_name = 'label', override_names=False, lattice: Lattice=None):
        self.graph_db = [IsoGraph(graph) for graph in graph_db]
        self.subgraphs = [IsoGraph(graph) for graph in subgraphs]
        self.label_name = label_name
        self._set_label_name()
        self.override_names = override_names
        # Rename subgraphs if necessary
        self._name_subgraphs()
        self.occurrences_transaction = {}
        self.occurrences_embeddings = {}
        self.occurrences_references = {}
        self.compressions = {}
        self.lattice = lattice
    
    def _set_label_name(self):
        for graph in self.graph_db:
            graph.set_label(self.label_name)
        for graph in self.subgraphs:
            graph.set_label(self.label_name)
        
    
    def _name_subgraphs(self):
        '''
        Simply given an identifier to graphs that don't have one yet.
        '''
        i = 0
        for graph in self.subgraphs:
            if self.override_names or graph.name is None or len(graph.name) == 0:
                graph.name=str(i)
                i+=1
    
    def compute_occurrences_brute_force(self):
        '''
        For a more efficient computation, see compute_occurrences_lattice_based.
        '''
        # for every subgraph count the occurrences in the graph db
        for subgraph in self.subgraphs:
            occurrences = 0
            for graph in self.graph_db:
                # todo count the occurrences instead of just "is there"
                if graph.contains(subgraph):
                    occurrences+=1
            self.occurrences_transaction[subgraph.name] = occurrences        
  
    def compute_occurrences_lattice_based(self):
        if self.lattice is None:
            # First create the lattice node for the subgraphs
            lattice_nodes = [LatticeNode(subgraph) for subgraph in self.subgraphs]
            # Create lattice (this might take some time)
            self.lattice = Lattice(lattice_nodes)
        # Execute occurrence computation
        self.lattice.count_occurrences(self.graph_db)
        # Store number of occurrences here
        for lattice_node in self.lattice.nodes:
            print(lattice_node.graph.name)
            self.occurrences_transaction[lattice_node.graph.name] = len(lattice_node.occurrences)
            # the occurrences of the nodes is just the id in the graph database, we want to get the names of the graphs instead
            self.occurrences_references[lattice_node.graph.name] = [self.graph_db[graph_id].name for graph_id in lattice_node.occurrences]
            # absolute compresion (heuristic)
            self.compressions[lattice_node.graph.name] = (len(lattice_node.occurrences)-1) * (len(lattice_node.graph.nodes()) + len(lattice_node.graph.edges()))

    def write_as_csv(self, save_path, additional_tag):
        '''
        Outputs occurrences statistics in a csv file.

        save_path: the path the csv is to be stored
        additional_tag: some additional tags to add, e.g., when later merging different results
        '''
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        
        with open(save_path, 'w', newline='') as csvfile:
            csvwriter = csv.writer(csvfile, delimiter=',',
                            quotechar='|', quoting=csv.QUOTE_MINIMAL)
            for subgraph in self.subgraphs:
                occurrences_embeddings = self.occurrences_embeddings[subgraph.name] if subgraph.name in self.occurrences_embeddings.keys() else 0
                occurrences_transaction = self.occurrences_transaction[subgraph.name] if subgraph.name in self.occurrences_transaction.keys() else 0
                occurrences_references = self.occurrences_references[subgraph.name] if subgraph.name in self.occurrences_references.keys() else []
                compression = self.compressions[subgraph.name] if subgraph.name in self.compressions.keys() else 0
                csvwriter.writerow([subgraph.name, additional_tag, occurrences_embeddings, occurrences_transaction, compression, occurrences_references])

    def write_as_md(self, save_path, project_name):
        os.makedirs(os.path.dirname(save_path), exist_ok=True)

        with open(save_path, 'w', newline='') as mdfile:
            for subgraph in self.subgraphs:
                mdfile.write(f"# {subgraph.name}\n")
                mdfile.write(f"![{subgraph.name}]({subgraph.name}.png)\n")
                occurrences_transaction = self.occurrences_transaction[subgraph.name] if subgraph.name in self.occurrences_transaction.keys() else 0
                occurrences_references = self.occurrences_references[subgraph.name] if subgraph.name in self.occurrences_references.keys() else []
                mdfile.write(f"Frequency: {occurrences_transaction}\n\n")
                mdfile.write("Put your notes here\n\n")
                mdfile.write("<details><summary>Matches</summary><p>\n")
                for occurrence in occurrences_references:
                    tokens = occurrence.split('$$$')
                    if not len(tokens) >= 2:
                        print(f"Illegal occurence string {occurence}")
                        continue
                    commit_id = tokens[1].strip()
                    file_path = tokens[0].strip()
                    # only works for commits, which are not merge commits
                    github_commit_url = get_url_for_project(project_name) + '/commit/'  + commit_id
                    mdfile.write(f"Commit: {github_commit_url}\n")
                    mdfile.write(f"File: {file_path}\n\n")
                mdfile.write("</p></details>\n\n")

def main(graph_db_path: str, subgraphs_path:str, results_dir:str):
    subgraphs = import_tlv_folder(subgraphs_path, parse_support=False)
    
    #TODO REMOVE THIS AGAIN, THIS IS ONLY TO FIT TO THE TEST DATA
    #subgraphs = [graph.reverse() for graph in subgraphs]
    
    #TODO Workaround since a dummy root has been added by a previous steps
    subgraphs = [IsoGraph(graph).cut_root() for graph in subgraphs]
        
    # Get rid of clones
    nb_initial_subgraphs = len(subgraphs)
    print("Removing duplicates. This might take some time...")
    subgraphs = remove_duplicates(subgraphs)
    nb_pruned_subgraphs = len(subgraphs)
    removed_duplicates = nb_initial_subgraphs - nb_pruned_subgraphs
    print("Removed %d duplicates" % removed_duplicates)
    
    print("Creating subgraph lattice for lattice-based counting...")
    # First create the lattice node for the subgraphs
    lattice_nodes = [LatticeNode(subgraph) for subgraph in subgraphs]
    # Create lattice (this might take some time)
    lattice = Lattice(lattice_nodes)  
    
    print("Exporting lattice.")
    nx_lattice = lattice.to_networkx()
    export_TLV([nx_lattice], results_dir + 'lattice.lg')
    #plot_graphs([nx_lattice], results_dir + 'lattice.png')
    #plot_graph_dot(nx_lattice, results_dir + 'lattice_dot.png')
    with open(results_dir + 'lattice.graphml', 'w') as f:
        f.write(lattice.to_graphml())
                       
    # Write subgraphs without clones
    print("Writing subgraphs without occurrences.")
    export_TLV(subgraphs, results_dir + 'subgraph_candidates.lg')

    for folder in os.listdir(graph_db_path):
        if not os.path.isdir(graph_db_path + "/" + folder + "/mining"):
            continue
        # Read db
        print(f"Parsing graph database for data set {folder}")
        graph_db = import_tlv_folder(graph_db_path+"/"+folder+"/mining/", parse_support=True)
        compute_statistics(graph_db, subgraphs, lattice, results_dir + "/" + folder + "/", folder)

def get_url_for_project(project): 
    ''' 
    Looks up the given project in a list of projects and returns the corresponding repository url.
    '''
    # TODO project list could be cached
    with open('project_list.md', 'r') as project_list:
        while True:
            line = project_list.readline()
            if not line:
                break
            tokens = line.split('|')
            # project name is token 0, repository url is token 4
            if tokens[0].strip() == project:
                return tokens[4]

def compute_statistics(graph_db, subgraphs, lattice, results_dir, dataset_name):
    # Compute statistics
    print("Counting the subgraph occurrences in the graph database. This might take some time...")
    stats = Statistics(graph_db, subgraphs, lattice=lattice)
    #start = time.time()
    #stats.compute_occurrences_brute_force()
    #print(stats.occurrences_transaction)
    #stop = time.time()
    #print("Computing occurrences brute force took %f seconds." % (stop-start))
    start = time.time()
    # TODO if this sill takes to long, think about some tricks, e.g., parallelizing,...
    stats.compute_occurrences_lattice_based()
    stop = time.time()
    print("Computing occurrences lattice based took %f seconds." % (stop-start))
    
    # Write statistics to file
    print("Write occurrence statistics...")
    stats.write_as_csv(results_dir + 'occurrence_stats.csv', dataset_name)
    stats.write_as_md(results_dir + 'occurrences_stats.md', dataset_name)

    print("Done")



  
if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Three arguments expected: path to graph database folder, path to subgraph database folder, path to results directory")
    
    # Create output folder if it doesn't exist yet
    os.makedirs(sys.argv[3], exist_ok=True)    
    main(sys.argv[1], sys.argv[2], sys.argv[3])
