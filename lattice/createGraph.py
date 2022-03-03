#!/usr/bin/python3

import graphviz

d = graphviz.Digraph(filename='createGraph.gv')
d.attr(overlap='false')
d.attr(compound='true')

patternsFile = open("patterns.lg", "r")
patternLines = patternsFile.readlines()

trees = {}
nodes = []
edges = []

for line in patternLines:
    line = line.replace("\n", "")
    line = line.replace("\r", "")
    
    if line.startswith("t"):
        # vorherigen Graph speichern
        if len(nodes):
            trees.update({tree : (nodes.copy(), edges.copy())})
            with d.subgraph(name=("cluster_" + tree)) as c:
                for v in nodes:
                    c.node(tree + "_" + v[0], tree + "_" + v[0])
                for e in edges:
                    c.edge(tree + "_" + e[0], tree + "_" + e[1])
        
        # neuen Graph einlesen
        nodes.clear()
        edges.clear()
        # t # TREE_ID
        lineParams = line.split(" ")
        tree = lineParams[2]
        
    elif line.startswith("v"):
        # v ID LABEL
        lineParams = line.split(" ")
        nodes.append((lineParams[1], lineParams[2]))
        
    elif line.startswith("e"):
        # e NODE_CHILD_ID NODE_PARENT_ID LABEL
        lineParams = line.split(" ")
        edges.append((lineParams[1], lineParams[2], lineParams[3]))

trees.update({tree : (nodes.copy(), edges.copy())})
with d.subgraph(name=("cluster_" + tree)) as c:
    for v in nodes:
        c.node(tree + "_" + v[0], tree + "_" + v[0])
    for e in edges:
        c.edge(tree + "_" + e[0], tree + "_" + e[1])



latticeFile = open("lattice.lg", "r")
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
        d.edge(latticeNodes.get(child) + "_0", latticeNodes.get(parent) + "_" + str(len(trees.get(latticeNodes.get(parent)))), ltail = "cluster_" + latticeNodes.get(child), lhead = "cluster_" + latticeNodes.get(parent))





#d.node('level0', 'Level')
#d.node('root', 'T')


#with d.subgraph() as s:
#    s.attr(rank='same')

d.view()
 
