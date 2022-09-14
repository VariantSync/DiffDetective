package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeTypedTreeDiff {
    DiffTree diffTree;
    List<RelationshipEdge<RelationshipType>> edges;

    HashMap<Class<? extends RelationshipType>, List<RelationshipEdge>> typeEdges;

    List<DiffNode> nodes;
    public EdgeTypedTreeDiff(DiffTree diffTree) {
        this.diffTree = diffTree;
        this.typeEdges = new HashMap<>();
        this.nodes = diffTree.computeAllNodes();
        for(DiffNode node : nodes){
            if (node.getAfterParent() == node.getBeforeParent()){
                if(node.getBeforeParent() != null) addEdge(new RelationshipEdge(UnchangedNesting.class, node, node.getBeforeParent()));
                continue;
            }
            if(node.getBeforeParent() != null) addEdge(new RelationshipEdge(BeforeNesting.class, node, node.getBeforeParent()));
            if(node.getAfterParent() != null) addEdge(new RelationshipEdge(AfterNesting.class, node, node.getAfterParent()));
        }
    }

    public void addEdgesWithType(Class<? extends RelationshipType> type, List<RelationshipEdge> edges){
        typeEdges.put(type, edges);
    }

    public void addEdges(List<RelationshipEdge> edges){
        for(RelationshipEdge edge : edges){
            addEdge(edge);
        }
    }

    public void addEdge(RelationshipEdge edge){
        if(!typeEdges.containsKey(edge.getType())) typeEdges.put(edge.getType(), new ArrayList<RelationshipEdge>());
        typeEdges.get(edge.getType()).add(edge);
    }
}
