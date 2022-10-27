package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EdgeTypedDiff {
    DiffTree diffTree;

    HashMap<Class<? extends RelationshipType>, List<RelationshipEdge<? extends RelationshipType>>> typedEdges;

    List<DiffNode> nodes;

    public DiffTree getDiffTree() {
        return diffTree;
    }

    public EdgeTypedDiff(DiffTree diffTree) {
        this.diffTree = diffTree;
        this.typedEdges = new HashMap<>();
        this.nodes = diffTree.computeAllNodes();
        for(DiffNode node : nodes){
            if (node.getAfterParent() == node.getBeforeParent()){
                if(node.getBeforeParent() != null) addEdge(new RelationshipEdge<>(UnchangedNesting.class, node, node.getBeforeParent()));
                continue;
            }
            if(node.getBeforeParent() != null) addEdge(new RelationshipEdge<>(BeforeNesting.class, node, node.getBeforeParent()));
            if(node.getAfterParent() != null) addEdge(new RelationshipEdge<>(AfterNesting.class, node, node.getAfterParent()));
        }
    }

    public void addEdgesWithType(Class<? extends RelationshipType> type, List<RelationshipEdge<? extends RelationshipType>> edges){
        if(typedEdges.containsKey(type)){
            typedEdges.get(type).addAll(edges);
        } else {
            typedEdges.put(type, edges);
        }
    }

    public void addEdges(List<RelationshipEdge<? extends RelationshipType>> edges){
        for(RelationshipEdge<? extends RelationshipType> edge : edges){
            addEdge(edge);
        }
    }

    public void addEdge(RelationshipEdge<? extends RelationshipType> edge){
        if(!typedEdges.containsKey(edge.getType())) typedEdges.put(edge.getType(), new ArrayList<>());
        typedEdges.get(edge.getType()).add(edge);
    }

    public List<RelationshipEdge<? extends RelationshipType>> getNestingEdges(){
        ArrayList<RelationshipEdge<? extends RelationshipType>> edges = new ArrayList<>();
        if(typedEdges.get(UnchangedNesting.class) != null) edges.addAll(typedEdges.get(UnchangedNesting.class));
        if(typedEdges.get(BeforeNesting.class) != null) edges.addAll(typedEdges.get(BeforeNesting.class));
        if(typedEdges.get(AfterNesting.class) != null)  edges.addAll(typedEdges.get(AfterNesting.class));
        return edges;
    }

    public List<RelationshipEdge<? extends RelationshipType>> getNonNestingEdges(){
        ArrayList<RelationshipEdge<? extends RelationshipType>> edges = new ArrayList<>();
        for(Class<? extends RelationshipType> type : typedEdges.keySet()){
            if(type != UnchangedNesting.class && type != BeforeNesting.class && type != AfterNesting.class && typedEdges.get(type) != null){
                edges.addAll(typedEdges.get(type));
            }
        }
        return edges;
    }

    public List<RelationshipEdge<? extends RelationshipType>> getAllEdgesOfType(Class<? extends RelationshipType> type){
        return typedEdges.get(type);
    }

    public int getNestingEdgeCount(){
        return getNestingEdges().size();
    }

    public int getNonNestingEdgeCount(){
        return getNonNestingEdges().size();
    }

    public float calculateAdditionalComplexity(){
        return ((float) getNonNestingEdgeCount())/((float) getNestingEdgeCount());
    }
}
