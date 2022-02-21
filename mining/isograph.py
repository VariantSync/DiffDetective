###############################################
###############################################
#### Module: Define IsoGraph class ############
### which handels isom. graph equality ########
############### and subgraph relation #########
###############################################

import networkx as nx
from typing import List
import networkx.algorithms.isomorphism as iso

def remove_duplicates(graphs: List[nx.DiGraph]) -> List[nx.DiGraph]:
    '''
    Removes duplicates from the given list.

    :param graphs: a list of networkx DiGraphs
    :returns: a list of networkx DiGraphs without duplicates (i.e., isomorphic graphs)
    '''
    # cast to IsoGraphs
    graphs = [IsoGraph(graph) for graph in graphs]
    return list(set(graphs))

class IsoGraph(nx.DiGraph):
   
    def set_label(self, label_name: str):
        self.label_name = label_name
    
    def __eq__(self, other: nx.DiGraph):
        if not hasattr(self, 'label_name') or self.label_name is None:
            self.label_name = "label"
            
        nm = iso.categorical_node_match(self.label_name, "")
        em = iso.categorical_edge_match(self.label_name, "")
        return nx.is_isomorphic(self, other, node_match=nm, edge_match=em)
        
    def __hash__(self):
        '''
        Computes a hash based on the Weisfeiler-Lehman test
        For a unique hash, something like the gSpan DFS code has to be used. This would be a complex operation.
        
        WL hash requires networkx version 2.6.2!
        '''
        return int(nx.weisfeiler_lehman_graph_hash(self), base=16)
        
    def contains(self, subgraph_candidate):
        nm = iso.categorical_node_match(self.label_name, "")
        em = iso.categorical_edge_match(self.label_name, "")
        GM = iso.DiGraphMatcher(self, subgraph_candidate, node_match=nm, edge_match=em)
        #DiGM.subgraph_monomorphisms_iter(
        return GM.subgraph_is_monomorphic()#, GM.mapping
        
    def cut_root(self):
        '''
        Removes the (single) root of the DiGraph and all outgoing edges. Fails if graph is not single-rooted.
        '''
        root = self.get_roots()
        assert len(root) == 1
        
        self.remove_nodes_from(root)
        return self
        
    def is_single_rooted(self) -> bool:
        return len(self.get_roots()) == 1
    
    def get_roots(self) -> List[int]:
        '''
        Computes all roots of a DiGraph.
        
        :return: a list of node_ids of the graph.
        '''
        # A root has in-degree = 0 
        return [n for n,d in self.in_degree() if d==0]
