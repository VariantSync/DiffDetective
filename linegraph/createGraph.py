#!/usr/bin/python3

import argparse
import graphviz

import graphGeneration as g

####################################################################
# How to
#
# Run script with ./createGraph
# Add arguments, such as
#     --patterns_path     path to the patterns file (default: ../lattice/patterns.lg)
#     --lattice_path      path to the lattice file (default: ../lattice/lattice.lg)
#     --node_parser       (default|patternsdebug|patternsrelease) 
#                         how the node labels in the patterns file should be interpreted
####################################################################

d = graphviz.Digraph(filename="lattice/createGraph.gv", engine='fdp')

# set border colour of the nodes
def nodeBorderColour(codetype):
    if codetype == g.CODETYPE_CODE:
        g.CODE_TYPE_CODE_COLOR
    else:
        g.CODE_TYPE_OTHER_COLOR


# the filled colour of the node
def nodeColour(difftype):
    if difftype == g.DIFFTYPE_NON:
        return g.DIFFTYPE_NON_COLOR
    elif difftype == g.DIFFTYPE_ADD:
        return g.DIFFTYPE_ADD_COLOR
    elif difftype == g.DIFFTYPE_REM:
        return g.DIFFTYPE_REM_COLOR

# draw a node within a sub graph
def drawNode(cluster, tree, nodeId, nodeLabel):
    nodedata = NODE_PARSER(nodeId, nodeLabel)

    # create node
    cluster.node(
        tree + "_" + nodeId, # the identifier of the node
        label = nodedata.label, # the label of the node
        color = nodeBorderColour(nodedata.codetype), # border colour of the node
        fillcolor = nodeColour(nodedata.difftype), style = "filled") # colour of the filled node
    #cluster.node_attr.update(height=10)


# draw an edge between node which are inside a sub graph
def drawEdge(cluster, tree, childNodeId, parentNodeId, nodeLabel):
    # create edge
    cluster.edge(
        tree + "_" + childNodeId, # identifier of the destination node (where the edge arrow points to)
        tree + "_" + parentNodeId, # identifier of the source node (where the edge arrow points from)
        color=g.edgeColour(nodeLabel)) # colour of the edge


# draw one tree, i.e. sub graph, of the patterns file
def drawCluster(tree, nodes, edges):
    with d.subgraph(name = ("cluster_" + tree)) as c: # add cluster/sub graph
        c.attr(label = "tree " + tree, overlap='false', sep="+10") # title of clusters
        # draw all nodes
        for v in nodes:
            drawNode(c, tree, v[0], v[1])
            # draw all edges
        for e in edges:
            drawEdge(c, tree, e[0], e[1], e[2])


# read in patterns file and read in vertices and edges
def patterns(patterns_file_path):
    patternsFile = open(patterns_file_path, "r")
    patternLines = patternsFile.readlines()

    trees = {} # contains all trees
    nodes = [] # contains all nodes of a tree
    edges = [] # contains all edges of a tree

    for line in patternLines:
        line = line.replace("\n", "")
        line = line.replace("\r", "")
        
        if line.startswith("t"):
            # save previous read tree
            if len(nodes):
                trees.update({tree : (nodes.copy(), edges.copy())})
                drawCluster(tree, nodes, edges)
            
            # read new tree (and clear all nodes and edges)
            nodes.clear()
            edges.clear()
            # t # TREE_ID
            lineParams = line.split(" ")
            tree = lineParams[2]
            
        # read in node
        elif line.startswith("v"):
            # v ID LABEL
            lineParams = line.split(" ")
            nodes.append((lineParams[1], ' '.join(lineParams[2:])))
        
        # read in edge
        elif line.startswith("e"):
            # e NODE_CHILD_ID NODE_PARENT_ID LABEL
            lineParams = line.split(" ")
            edges.append((lineParams[1], lineParams[2], lineParams[3]))

    # repeat saving process for the last tree
    trees.update({tree : (nodes.copy(), edges.copy())})
    drawCluster(tree, nodes, edges)


# read in lattice file, i.e. the connections between all subgraphs
def lattice(lattice_file_path):
    latticeFile = open(lattice_file_path, "r")
    latticeLines = latticeFile.readlines()

    latticeNodes = {}
    #latticeEdges = []

    for line in latticeLines:
        line = line.replace("\n", "")
        line = line.replace("\r", "")
        
        if line.startswith("t"):
            # t # TREE_ID
            pass
        
        elif line.startswith("v"):
            # v ID LABEL
            lineParams = line.split(" ")
            latticeNodes.update({lineParams[1]: lineParams[2]})

            
        elif line.startswith("e"):
            # e NODE_CHILD_ID NODE_PARENT_ID LABEL
            lineParams = line.split(" ")
            child = lineParams[1]
            parent = lineParams[2]
    #        latticeEdges.append((child, parent))
            d.edge("cluster_" + latticeNodes.get(child), "cluster_" + latticeNodes.get(parent))
            #d.edge(latticeNodes.get(child) + "_0", latticeNodes.get(parent) + "_" + str(len(trees.get(latticeNodes.get(parent)))), ltail = "cluster_" + latticeNodes.get(child), lhead = "cluster_" + latticeNodes.get(parent))

NODE_PARSER = g.parseNodeReleaseAtomics

def main():
    # get parameters
    argparser = argparse.ArgumentParser()
    argparser.add_argument('--patterns_path', nargs='?', default="../lattice/patterns.lg", type=str)
    argparser.add_argument('--lattice_path', nargs='?', default="../lattice/lattice.lg", type=str)
    argparser.add_argument('--node_parser', nargs='?', default="patternsrelease", type=str)
    args = argparser.parse_args()
    
    patterns_path = args.patterns_path
    lattice_path = args.lattice_path
    
    # select the node parser
    global NODE_PARSER # accessing the gloabl variable NODE_PARSER
    if args.node_parser == "default":
        NODE_PARSER = g.parseNodeDefault
    elif args.node_parser == "patternsdebug":
        NODE_PARSER = g.parseNodeDebugAtomics
    elif args.node_parser == "patternsrelease":
        NODE_PARSER = g.parseNodeReleaseAtomics
    else:
        print("Node parser type does not exist.")

    #d.attr(rankdir='LR')
    d.attr(overlap='false')
    #d.attr(compound='true')
    
    d.attr(sep = "+10")
    
    patterns(patterns_path)
    lattice(lattice_path)
    
    d.view()


if __name__ == "__main__":
    main()
