package org.variantsync.diffdetective.experiments.thesis_pm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.util.fide.FixTrueFalse.Formula;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

public class PatchingExperiment {
	
	private static Relevance calculateFeatureSetToDeselect(VariationTree<DiffLinesLabel> variant1, VariationTree<DiffLinesLabel> variant2, boolean debug) {
		Set<String> featuresTreeV1 = new HashSet<String>();
        variant1.forAllPreorder(node -> {featuresTreeV1.addAll(node.getFeatureMapping().getUniqueContainedFeatures());});
        Set<String> featuresTreeV2 = new HashSet<String>();
        variant2.forAllPreorder(node -> {featuresTreeV2.addAll(node.getFeatureMapping().getUniqueContainedFeatures());});
        
        Set<String> intersectSet1 = new HashSet<>(featuresTreeV1);
        intersectSet1.removeAll(featuresTreeV2);
        Set<String> intersectSet2 = new HashSet<>(featuresTreeV2);
        intersectSet2.removeAll(featuresTreeV1);
        intersectSet1.addAll(intersectSet2);
        
        if (debug) {
        	System.out.println(featuresTreeV1);
            System.out.println(featuresTreeV2);
            System.out.println(intersectSet1);
        }
        
        
        Formula[] f = new Formula[intersectSet1.size()];
        Iterator<String> iterator = intersectSet1.iterator();
        for (int i = 0; i < f.length; i++) {
        	f[i] = Formula.not(Formula.var(iterator.next()));
        }
        Formula formula = Formula.and(f);
        
        if (debug) System.out.println(formula.get().toString());
        
        Relevance rho = new Configure(formula);
        return rho;
	}
	
	private static Set<DiffNode<DiffLinesLabel>> findRootsOfSubtrees(Set<DiffNode<DiffLinesLabel>> nodes, boolean debug) {
		Set<DiffNode<DiffLinesLabel>> subtreeRoots = new HashSet<DiffNode<DiffLinesLabel>>();
        for (DiffNode<DiffLinesLabel> node : nodes) {
        	if (!nodes.contains(node.getParent(Time.AFTER))) {
        		subtreeRoots.add(node);
        	}
        }
        if (debug) System.out.println(subtreeRoots);
        
        return subtreeRoots;
	}
	
	private static void patchVariationTrees(VariationTree<DiffLinesLabel> sourceVariantVersion1, VariationTree<DiffLinesLabel> sourceVariantVersion2, VariationTree<DiffLinesLabel> targetVariant) {
		if (sourceVariantVersion1 == null || sourceVariantVersion2 == null || targetVariant == null) {
			System.out.println("Parsing error");
			return;
		}
		
		VariationDiff<DiffLinesLabel> diff = VariationDiff.fromTrees(sourceVariantVersion1, sourceVariantVersion2);
		Relevance rho = calculateFeatureSetToDeselect(sourceVariantVersion1, targetVariant, false);
        VariationDiff<DiffLinesLabel> optimizedDiff = DiffView.optimized(diff, rho);
        
        // add new nodes
        Set<DiffNode<DiffLinesLabel>> addedNodes = new HashSet<DiffNode<DiffLinesLabel>>();
        optimizedDiff.forAll(node -> {if (node.isAdd()) {addedNodes.add(node);}});
        
        // find roots of subtrees of added nodes
        Set<DiffNode<DiffLinesLabel>> addedSubtreeRoots = findRootsOfSubtrees(addedNodes, false);
        
        VariationDiff<DiffLinesLabel> targetVariantDiff = VariationDiff.fromTrees(targetVariant, targetVariant);
        for (DiffNode<DiffLinesLabel> root : addedSubtreeRoots) {
        	VariationDiff<DiffLinesLabel> subTree = new VariationDiff<DiffLinesLabel>(root.deepCopy(), optimizedDiff.getSource());
        	GameEngine.showAndAwaitAll(Show.diff(subTree));
        	List<DiffNode<DiffLinesLabel>> targetNodes = new ArrayList<DiffNode<DiffLinesLabel>>();
        	if (root.isArtifact()) {
        		targetNodes = targetVariantDiff.computeAllNodesThat(node -> node.getPresenceCondition(Time.AFTER)
            			.equals(root.getPresenceCondition(Time.AFTER)) && node.isAnnotation());
        	} else if (root.isAnnotation()) {
        		targetNodes = targetVariantDiff.computeAllNodesThat(node -> node.getPresenceCondition(Time.AFTER)
            			.equals(root.getParent(Time.AFTER).getPresenceCondition(Time.AFTER)) && node.isAnnotation());
        	}
        	
        	if (targetNodes.size() != 1) {
        		System.out.println("too much or too less target nodes found");
        	} else {
        		System.out.println("subtree added");
        		targetNodes.get(0).addChild(root.deepCopy(), Time.AFTER);
        	}
        }
        
        // remove old nodes
        Set<DiffNode<DiffLinesLabel>> removedNodes = new HashSet<DiffNode<DiffLinesLabel>>();
        optimizedDiff.forAll(node -> {if (node.isRem()) {removedNodes.add(node);}});
        
        Set<DiffNode<DiffLinesLabel>> removedSubtreeRoots = findRootsOfSubtrees(removedNodes, false);
        
//        VariationDiff<DiffLinesLabel> targetVariantDiff2 = VariationDiff.fromTrees(targetVariant, targetVariant);
        for (DiffNode<DiffLinesLabel> root : removedSubtreeRoots) {
        	VariationDiff<DiffLinesLabel> subTree = new VariationDiff<DiffLinesLabel>(root.deepCopy(), optimizedDiff.getSource());
        	GameEngine.showAndAwaitAll(Show.diff(subTree));
        	List<DiffNode<DiffLinesLabel>> targetNodes = new ArrayList<DiffNode<DiffLinesLabel>>();
        	if (root.isArtifact()) {
        		targetNodes = targetVariantDiff.computeAllNodesThat(node -> node.getPresenceCondition(Time.AFTER)
            			.equals(root.getPresenceCondition(Time.BEFORE)) && node.isAnnotation());
        	} else if (root.isAnnotation()) {
        		targetNodes = targetVariantDiff.computeAllNodesThat(node -> node.getPresenceCondition(Time.AFTER)
            			.equals(root.getParent(Time.BEFORE).getPresenceCondition(Time.BEFORE)) && node.isAnnotation());
        	}
        	if (targetNodes.size() != 1) {
        		System.out.println("too much or too less target nodes found");
        	} else {
        		
//        		System.out.println(targetNodes.get(0));
        		DiffNode<DiffLinesLabel> parent = targetNodes.get(0);
        		List<DiffNode<DiffLinesLabel>> nodesToRem = new ArrayList<DiffNode<DiffLinesLabel>>();
        		parent.getAllChildrenStream().forEach(node -> { if (node.isSameAs(root, Time.BEFORE)) nodesToRem.add(node);});
        		if (nodesToRem.size() != 1) {
        			System.out.println("too much or too less target nodes found");
        		} else {
        			System.out.println("subtree removed");
        			nodesToRem.get(0).diffType = DiffType.REM;
        			nodesToRem.get(0).drop(Time.AFTER);
        		}
        	}
        }
        
        GameEngine.showAndAwaitAll(
      	      Show.tree(sourceVariantVersion1),
      	      Show.tree(sourceVariantVersion2),
      	      Show.tree(targetVariant),
      	      Show.diff(optimizedDiff),
      	      Show.diff(targetVariantDiff)
      	    );
        
	}
	
	private static VariationTree<DiffLinesLabel> parseVariationTreeFromFile(String file) {
		Path examplesDir = Path.of("data", "examples");
        Path path  = examplesDir.resolve(file);
        try {
			VariationTree<DiffLinesLabel> tree = VariationTree.fromFile(
			        path,
			        VariationDiffParseOptions.Default
			);
			return tree;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DiffParseException e) {
			e.printStackTrace();
		}
        return null;
	}
	
	public static void main(String[] args) {
		patchVariationTrees(parseVariationTreeFromFile("exampleA1.cpp"), parseVariationTreeFromFile("exampleA2.cpp"), parseVariationTreeFromFile("exampleB.cpp"));
	}

}
