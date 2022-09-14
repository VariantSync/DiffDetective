package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeTypedTreeDiff {
    DiffTree diffTree;
    List<RelationshipEdge<RelationshipType>> edges;

    HashMap<Class<? extends RelationshipType>, List<RelationshipEdge>> typeEdges;
    public EdgeTypedTreeDiff(DiffTree diffTree) {
        this.diffTree = diffTree;
        this.typeEdges = new HashMap<>();
        // TODO: split diffTree into vertices and edges here
    }

    public void addEdgesWithType(Class<? extends RelationshipType> type, List<RelationshipEdge> edges){
        typeEdges.put(type, edges);
    }

    public void addEdges(List<RelationshipEdge> edges){
        for(RelationshipEdge edge : edges){
            typeEdges.get(edge.getType()).add(edge);
        }
    }
}
