package org.variantsync.diffdetective.variation.diff.patching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.experiments.thesis_pm.Generator;
import org.variantsync.diffdetective.experiments.thesis_pm.Utils;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.diff.transform.CutNonEditedSubtrees;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.relevance.Configure;
import org.variantsync.diffdetective.variation.tree.view.relevance.ConfigureWithFullConfig;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;
import org.variantsync.diffdetective.variation.tree.view.relevance.Trace;
import org.variantsync.diffdetective.variation.tree.view.relevance.TraceSup;

public class Patching {
	
	/**
	 * Helper-function to collect all roots of the changed subtrees.
	 * 
	 * @param nodes The {@link DiffNode} to consider
	 * @param type The {@link DiffType} to consider
	 * @param debug Flag to print debug statements
	 * @return A Set of {@link DiffNode}s that represent the roots of the changed subtrees.
	 */
	private static Set<DiffNode<DiffLinesLabel>> findRootsOfSubtrees(Set<DiffNode<DiffLinesLabel>> nodes, DiffType type,
			boolean debug) {
		Time time = (type == DiffType.ADD) ? Time.AFTER : Time.BEFORE;
		Set<DiffNode<DiffLinesLabel>> subtreeRoots = new HashSet<DiffNode<DiffLinesLabel>>();
		for (DiffNode<DiffLinesLabel> node : nodes) {
			if (!nodes.contains(node.getParent(time))) {
				subtreeRoots.add(node);
			}
		}
		if (debug)
			System.out.println(subtreeRoots);

		return subtreeRoots;
	}
	
	/**
	 * Helper-function to compare all ancestors from the given node to the root recursively. Checks equality by
	 * comparing the labels of the nodes.
	 * 
	 * @param node1 {@link DiffNode} to compare 
	 * @param node2 {@link DiffNode} to compare
	 * @param time The given {@link Time}
	 * @return True, if the ancestors are equal, false otherwise.
	 */
	private static boolean compareAncestors(DiffNode<DiffLinesLabel> node1, DiffNode<DiffLinesLabel> node2, Time time) {
		if (node1.getParent(time) == null && node2.getParent(time) == null)
			return true;
		if (node1.getParent(time) != null && node2.getParent(time) == null)
			return false;
		if (node1.getParent(time) == null && node2.getParent(time) != null)
			return false;
		List<DiffNode<DiffLinesLabel>> siblingsNode1 = node1.getParent(time).getChildOrder(time);
		List<DiffNode<DiffLinesLabel>> siblingsNode2 = node2.getParent(time).getChildOrder(time);

		int indexNode1 = siblingsNode1.indexOf(node1);
		int indexNode2 = siblingsNode2.indexOf(node2);
		if (indexNode1 != indexNode2) {
			return false;
		}
		int index2 = 0;
		for (int i = 0; i < siblingsNode1.size(); i++) {
			if (i > indexNode1 && siblingsNode1.get(i).getDiffType() == DiffType.ADD) {
				continue;
			}
//			if (i > index2 && siblingsNode2.get(index2).getDiffType() == DiffType.REM)
			if (!Utils.hasSameLabel(siblingsNode1.get(i).getLabel(), siblingsNode2.get(index2).getLabel())) {
				return false;
			}
			index2++;
		}
		return compareAncestors(node1.getParent(time), node2.getParent(time), time, debug);
	}
	
	/**
	 * Helper-function to compare all ancestors from the given node to the root recursively.
	 * 
	 * @param root The root of the changed subtree as {@link DiffNode} originating the source patch
	 * @param targetNodeInPatch The target {@link DiffNode} to which the change should be applied
	 * @param time The given {@link Time}
	 * @return True, if the ancestors are equal, false otherwise.
	 */
	private static boolean compareAncestorsToRoot(DiffNode<DiffLinesLabel> root,
			DiffNode<DiffLinesLabel> targetNodeInPatch, Time time) {
		if (root.getParent(time) == null && targetNodeInPatch == null) {
			return true;
		}
		if ((root.getParent(time) != null && targetNodeInPatch == null)
				|| (root.getParent(time) == null && targetNodeInPatch != null)) {
			return false;
		}
		return compareAncestors(root.getParent(time), targetNodeInPatch, time);
	}
	
	/**
	 * Helper-function to compare two lists of nodes. They are equal if all nodes of the sourceList
	 * are also in the targetList, and if all unmatched nodes of the targetList are not present under
	 * the configuration of the source variant.
	 * 
	 * @param sourceList A list of {@link DiffNode}s corresponding to the source variant
	 * @param targetList A list of {@link DiffNode}s corresponding to the target variant
	 * @param configSource The configuration of the source variant as {@link ConfigureWithFullConfig}
	 * @return True, if the lists are equal, otherwise false.
	 */
	private static boolean isSameList(List<DiffNode<DiffLinesLabel>> sourceList,
			List<DiffNode<DiffLinesLabel>> targetList, ConfigureWithFullConfig configSource) {
		int indexTarget = 0;
		// source must be equal or smaller than target list, because it is the view on
		// the source patch (cross variant features)
		if (sourceList.size() > targetList.size()) {
			return false;
		}
		if (sourceList.size() == 0) {
			if (targetList.size() == 0) {
				return true;
			}
			// check that all nodes in target list are not present in source
			for (DiffNode<DiffLinesLabel> targetNode : targetList) {
				if (isPresentUnderConfiguration(targetNode, configSource)) {
					return false;
				}
			}
			return true;
		}
		for (DiffNode<DiffLinesLabel> sourceNode : sourceList) {
			if (indexTarget >= targetList.size()) {
				return false;
			}
			while (!isPresentUnderConfiguration(targetList.get(indexTarget), configSource)) {
				indexTarget++;
				if (indexTarget >= targetList.size()) {
					return false;
				}
			}
			DiffNode<DiffLinesLabel> targetNode = targetList.get(indexTarget);
			if (!Utils.hasSameLabel(sourceNode.getLabel(), targetNode.getLabel())) {
				return false;
			}
			indexTarget++;
		}
		while (indexTarget != targetList.size()) {
			if (isPresentUnderConfiguration(targetList.get(indexTarget), configSource)) {
				return false;
			}
			indexTarget++;
		}
		return true;
	}
	
	/**
	 * Helper-function to check if a node is present under the given full configuration.
	 * 
	 * @param diffNode The {@link DiffNode} to check
	 * @param config The configuration as {@link ConfigureWithFullConfig}
	 * @return True, if the node is present under the given configuration, false otherwise.
	 */
	private static boolean isPresentUnderConfiguration(DiffNode<DiffLinesLabel> diffNode, ConfigureWithFullConfig config) {
		return config.test(diffNode.projection(Time.BEFORE));
	}
	
	/**
	 * Checks the neighbors before and after the target node compared to the parent node of the root to find the
	 * correct node where to apply the change.
	 * 
	 * @param root The root of the subtree {@link DiffNode} that should be changed.
	 * @param targetNodeInPatch The node as {@link DiffNode} to/from which the subtree should be added/removed.
	 * @param configSource The configuration as {@link ConfigureWithFullConfig} of the source variant
	 * @param time The given {@link Time}
	 * @return The node to which the change must be applied
	 * @throws Exception if no unique candidate node was found
	 */
	private static DiffNode<DiffLinesLabel> checkNeighbors(DiffNode<DiffLinesLabel> root,
			DiffNode<DiffLinesLabel> targetNodeInPatch, ConfigureWithFullConfig configSource, Time time)
			throws Exception {
		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);
		int indexSource = orderedChildrenSource.indexOf(root);
		List<DiffNode<DiffLinesLabel>> candidates = new ArrayList<>();
		for (DiffNode<DiffLinesLabel> node : orderedChildrenTarget) {
			if (!Utils.hasSameLabel(node.getLabel(), root.getLabel())) {
				continue;
			}
			int indexTarget = orderedChildrenTarget.indexOf(node);
			
			List<DiffNode<DiffLinesLabel>> neighborsBeforeSource = orderedChildrenSource.subList(0, indexSource);
			List<DiffNode<DiffLinesLabel>> neighborsAfterSource = orderedChildrenSource.subList(indexSource + 1,
					orderedChildrenSource.size());
			List<DiffNode<DiffLinesLabel>> neighborsBeforeTarget = orderedChildrenTarget.subList(0, indexTarget);
			List<DiffNode<DiffLinesLabel>> neighborsAfterTarget = orderedChildrenTarget.subList(indexTarget + 1,
					orderedChildrenTarget.size());
			if (isSameList(neighborsBeforeSource, neighborsBeforeTarget, configSource)
					&& isSameList(neighborsAfterSource, neighborsAfterTarget, configSource)) {
				candidates.add(node);
			}
		}
		if (candidates.size() != 1) {
			throw new Exception("Reject: too many nodes to remove: " + candidates.size());
		}
		return candidates.get(0);
	}
	
	/**
	 * Finds the correct insertion position of the given subtree of type ADD.
	 * 
	 * @param root The root of the given subtree as {@link DiffNode}
	 * @param targetNodeInPatch The parent node in the source patch as {@link DiffType}
	 * @param targetNodeInPatchView The parent node in the view of the target patch configured with the source configuration
	 * @param time The {@link Time} to consider
	 * @return The insertion position as {@link Integer}
	 * @throws Exception throws an exception if no insert position could be found.
	 */
	private static int findInsertPosition(DiffNode<DiffLinesLabel> root, DiffNode<DiffLinesLabel> targetNodeInPatch,
			DiffNode<DiffLinesLabel> targetNodeInPatchView, Time time) throws Exception {
		List<DiffNode<DiffLinesLabel>> orderedChildrenTarget = targetNodeInPatch.getChildOrder(time);
		List<DiffNode<DiffLinesLabel>> orderedChildrenSource = root.getParent(time).getChildOrder(time);
		int indexSource = orderedChildrenSource.indexOf(root);
		int indexTarget = 0;
		int insertPosition = indexSource;
		for (int i = 0; i < orderedChildrenSource.size(); i++) {
			if (i == indexSource) {
				insertPosition = indexTarget;
				continue;
			}
			if (i > indexSource && orderedChildrenSource.get(i).getDiffType() == DiffType.ADD) {
				continue;
			}
			if (!Utils.hasSameLabel(orderedChildrenSource.get(i).getLabel(),
					orderedChildrenTarget.get(indexTarget).getLabel())) {
				while (!Utils.hasSameLabel(orderedChildrenSource.get(i).getLabel(),
						orderedChildrenTarget.get(indexTarget).getLabel())) {
					indexTarget++;
					if (indexTarget >= orderedChildrenTarget.size()) {
						throw new Exception("could not find insert position");
					}
				}
			}
			indexTarget++;
		}
		return insertPosition;
	}
	
	/**
	 * Applies all sorted changes of a given type to the target variant. The changes are group as subtrees. 
	 * 
	 * @param type The given {@link DiffType} 
	 * @param targetVariantDiffUnchanged A copy of the unchanged target variant as {@link VariationDiff}
	 * @param targetVariantDiffPatched A copy of the current state of the patch
	 * @param subtreeRoots A list of {@link DiffNode} that represent the roots of the subtrees that should be
	 * 						applied to the target variant.
	 * @param source The source as {@link VariationDiffSource}
	 * @param configSource The configuration of the source variant as {@link ConfigureWithFullConfig}
	 * @param debug Flag to print and show debug steps with GUI {@link GameEngine}.
	 * @throws Exception Throws an Exception if one or more changes could not be applied to the target variant.
	 */
	private static void applyChanges(DiffType type, VariationDiff<DiffLinesLabel> targetVariantDiffUnchanged,
			VariationDiff<DiffLinesLabel> targetVariantDiffPatched, List<DiffNode<DiffLinesLabel>> subtreeRoots,
			VariationDiffSource source, ConfigureWithFullConfig configSource, boolean debug) throws Exception {

		Time time = (type == DiffType.ADD) ? Time.AFTER : Time.BEFORE;

		for (DiffNode<DiffLinesLabel> root : subtreeRoots) {
			if (debug) {
				DiffNode<DiffLinesLabel> newRoot = DiffNode.createRoot(new DiffLinesLabel());
				newRoot.addChild(root.deepCopy(), time);
				VariationDiff<DiffLinesLabel> subTree = new VariationDiff<DiffLinesLabel>(newRoot, source);
				GameEngine.showAndAwaitAll(Show.diff(subTree));
			}

			List<DiffNode<DiffLinesLabel>> targetNodes = new ArrayList<DiffNode<DiffLinesLabel>>();

			if (root.getParent(time).getDiffType().existsAtTime(time)) {
				final Node presenceCondition = root.getParent(time).getPresenceCondition(time);
				targetNodes = targetVariantDiffUnchanged.computeAllNodesThat(
						node -> node.getPresenceCondition(Time.AFTER).equals(presenceCondition) && node.isAnnotation());
			}

			VariationDiff<DiffLinesLabel> targetVariantDiffPatchedView = DiffView
					.optimized(targetVariantDiffPatched.deepCopy(), configSource);
			targetNodes = targetNodes.stream().filter(targetNode -> compareAncestorsToRoot(root,
					targetVariantDiffPatchedView.getNodeWithID(targetNode.getID()), time)).toList();
			targetNodes = targetNodes.stream()
					.map(targetNode -> targetVariantDiffPatched.getNodeWithID(targetNode.getID())).toList();
			if (targetNodes.size() != 1) {
				throw new Exception("too much or too less target nodes after filtering: " + targetNodes.size());
			}

			DiffNode<DiffLinesLabel> targetNodeInPatch = targetNodes.get(0);
			if (debug)
				System.out.println(targetNodeInPatch.toString());
			if (type == DiffType.ADD) {
				if (debug) {
					GameEngine.showAndAwaitAll(Show.tree(targetVariantDiffPatched.project(Time.AFTER)));
				}
				int insertPosition = findInsertPosition(root, targetNodeInPatch,
						targetVariantDiffPatchedView.getNodeWithID(targetNodeInPatch.getID()), time);
				if (insertPosition < 0) {
					if (debug)
						System.out.println("no matching insert position found");
				}
				if (debug)
					System.out.println("subtree added");
				targetNodeInPatch.insertChild(root.deepCopy(), insertPosition, time);
				if (debug)
					System.out.println(targetNodeInPatch.getChildOrder(time));

			} else if (type == DiffType.REM) {
				DiffNode<DiffLinesLabel> nodesToRem = checkNeighbors(root, targetNodeInPatch, configSource, time);
				if (debug)
					System.out.println("Nodes to remove: " + nodesToRem);

				if (debug)
					System.out.println("subtree removed");
				DiffNode<DiffLinesLabel> newRoot = DiffNode.createRoot(new DiffLinesLabel());
				newRoot.addChild(root.deepCopy(), time);
				VariationDiff<DiffLinesLabel> subTree = new VariationDiff<DiffLinesLabel>(newRoot, source);
				DiffNode<DiffLinesLabel> newRoot2 = DiffNode.createRoot(new DiffLinesLabel());
				newRoot2.addChild(nodesToRem.deepCopy(), time);
				VariationDiff<DiffLinesLabel> subTreeB = new VariationDiff<DiffLinesLabel>(newRoot2,
						targetVariantDiffPatchedView.getSource());
				removeNode(subTreeB.project(time), targetVariantDiffPatched, subTree);

				if (debug)
					System.out.println(targetNodeInPatch.getChildOrder(Time.AFTER));
			}
			if (debug) {
				VariationDiff<DiffLinesLabel> targetVariantDiffPatchedCopy = targetVariantDiffPatched.deepCopy();
//				CutNonEditedSubtrees.genericTransform(targetVariantDiffPatchedCopy);
				GameEngine.showAndAwaitAll(Show.diff(targetVariantDiffPatchedCopy));
			}
		}
	}

	/**
	 * Helper-function to determine if all children nodes are planned to be removed. Checks whether all children
	 * are contained in the given list of ids of those nodes that should be removed. 
	 * 
	 * @param children The list of {@link VariationTreeNode} that should be checked
	 * @param idsToRemove The list of Ids of nodes that should be removed.
	 * @return Returns if all children are contained in the id list of nodes that should be removed.
	 */
	private static boolean areAllChildrenPlannedToRemove(List<VariationTreeNode<DiffLinesLabel>> children,
			List<Integer> idsToRemove) {
		for (VariationTreeNode<DiffLinesLabel> child : children) {
			if (!idsToRemove.contains(child.getID())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Removes a given node and its children from the {@link VariationDiff}. Compares nodes of the source and the
	 * target variant by calculated identifier.
	 * 
	 * @param node The given node and its children as subtree and therefore as {@link VariationTree}
	 * @param diffToRemoveFrom The current state of the target variant's patch as {@link VariationDiff}
	 * @param subtree The subtree from the source patch.
	 * @throws Exception Throws an exception if a node cannot be removed.
	 */
	private static void removeNode(VariationTree<DiffLinesLabel> node, VariationDiff<DiffLinesLabel> diffToRemoveFrom,
			VariationDiff<DiffLinesLabel> subtree) throws Exception {
		List<Integer> idsToRemove = new ArrayList<>();
		// Nodes with these labels and pc should be removed
		List<String> labelAndPC = new ArrayList<>();
		subtree.forAll(n -> {
			if (!n.isRoot()) {
				String identifier = calcIdentifier(n);
				labelAndPC.add(identifier);
			}
		});

		node.forAllPostorder(n -> {
			String identifier = calcIdentifier(n);
			// this node should be removed, but it can only be removed if it does not have
			// any other children
			if (labelAndPC.contains(identifier)) {
				// n is leaf or all children should be removed
				if (n.isLeaf() || areAllChildrenPlannedToRemove(n.getChildren(), idsToRemove)) {
					idsToRemove.add(n.getID());
				}
			}
		});
		for (Integer id : idsToRemove) {
			DiffNode<DiffLinesLabel> n = diffToRemoveFrom.getNodeWithID(id);
//			n.diffType = DiffType.REM;
			if (n.getParent(Time.AFTER) != null) {
				n.drop(Time.AFTER);
			}
		}
	}
	
	/**
	 * Helper-Function to calculate identifier from the labels.
	 * 
	 * @param n The {@link VariationTreeNode} to calculate the identifier for.
	 * @return The identifier as String.
	 */
	private static String calcIdentifier(VariationTreeNode<DiffLinesLabel> n) {
		String identifier = "";
		List<String> l = n.getLabel().getLines();
		if (l != null) {
			for (String line : l) {
				identifier += line;
			}
		}
		List<String> tl = n.getLabel().getTrailingLines();
		if (tl != null) {
			for (String line : tl) {
				identifier += line;
			}
		}
		Node pc = n.getPresenceCondition();
		if (pc != null) {
			identifier += pc.toString();
		}
		return identifier;
	}

	/**
	 * Helper-Function to calculate identifier from the labels.
	 * @param n The {@link DiffNode} to calculate the identifier for.
	 * @return The identifier as String.
	 */
	private static String calcIdentifier(DiffNode<DiffLinesLabel> n) {
		String identifier = "";
		List<String> l = n.getLabel().getLines();
		if (l != null) {
			for (String line : l) {
				identifier += line;
			}
		}
		List<String> tl = n.getLabel().getTrailingLines();
		if (tl != null) {
			for (String line : tl) {
				identifier += line;
			}
		}
		Node pc = n.getPresenceCondition(Time.BEFORE);
		if (pc != null) {
			identifier += pc.toString();
		}
		return identifier;
	}
	
	/**
	 * Helper-function to change the type of a node in the given variation diff to the given type.
	 * 
	 * @param node The {@link DiffNode} whose type should be changed
	 * @param modDiff The {@link VariationDiff} that contains the node.
	 * @param type The given {@link DiffType} to which the node type should be changed.
	 */
	private static void changeType(DiffNode<DiffLinesLabel> node, VariationDiff<DiffLinesLabel> modDiff, DiffType type) {
		if (!node.isLeaf()) {
			node.getAllChildrenStream().forEach(child -> changeType(child, modDiff, type));
		}
		DiffNode<DiffLinesLabel> matchingNode = modDiff.getNodeWithID(node.getID());
		if (matchingNode == null) {
			return;
		}
		Time time = type == DiffType.ADD ? Time.BEFORE : Time.AFTER;
		if (matchingNode.isNon()) {
			matchingNode.diffType = type;
			matchingNode.drop(time);
		} else if (matchingNode.diffType != type) {
			matchingNode.drop();
		}
	}
	
	/**
	 * In the patch, there can be nodes of type NON that actually were modified as they were moved in the code. So, these
	 * nodes have different parents at time before and after. As this simplification of the {@link VariationDiff} is problematic
	 * for our patching algorithm, we split such a node and its children to a node of type removed and a node of type added.
	 * 
	 * @param node The {@link DiffNode} that should be split
	 * @param modDiff The current {@link VariationDiff} in which node is contained
	 */
	private static void resolve(DiffNode<DiffLinesLabel> node, VariationDiff<DiffLinesLabel> modDiff) {
		if (!node.isLeaf()) {
			node.getAllChildrenStream().forEach(child -> resolve(child, modDiff));
		}
		if (node.isNon() && node.getParent(Time.BEFORE) != node.getParent(Time.AFTER)) {
//			System.out.println(node.getLabel());
			DiffNode<DiffLinesLabel> matchingNode = modDiff.getNodeWithID(node.getID());
			// matchingNode can be null because it is a child from two parents
			if (matchingNode == null) {
				return;
			}
			DiffNode<DiffLinesLabel> parentAfter = matchingNode.getParent(Time.AFTER);
			DiffNode<DiffLinesLabel> parentBefore = matchingNode.getParent(Time.BEFORE);
			int indexAfter = parentAfter.getChildOrder(Time.AFTER).indexOf(matchingNode);
			int indexBefore = parentBefore.getChildOrder(Time.BEFORE).indexOf(matchingNode);
			matchingNode.drop();
			DiffNode<DiffLinesLabel> nodeAfter = matchingNode.deepCopy();
			nodeAfter.diffType = DiffType.ADD;

			DiffNode<DiffLinesLabel> newRootAfter = DiffNode.createRoot(new DiffLinesLabel());
			newRootAfter.addChild(nodeAfter.deepCopy(), Time.AFTER);
			VariationDiff<DiffLinesLabel> subTreeAfter = new VariationDiff<DiffLinesLabel>(newRootAfter,
					modDiff.getSource());

			if (!nodeAfter.isLeaf()) {
				changeType(nodeAfter, subTreeAfter, DiffType.ADD);
				// remove all children with difftype REM recursively
				// set recursively difftype ADD for all children which have currently difftype
				// NON
			}

//			GameEngine.showAndAwaitAll(Show.diff(subTreeAfter));

			DiffNode<DiffLinesLabel> nodeBefore = matchingNode.deepCopy();
			nodeBefore.diffType = DiffType.REM;

			DiffNode<DiffLinesLabel> newRootBefore = DiffNode.createRoot(new DiffLinesLabel());
			newRootBefore.addChild(nodeBefore.deepCopy(), Time.BEFORE);
			VariationDiff<DiffLinesLabel> subTreeBefore = new VariationDiff<DiffLinesLabel>(newRootBefore,
					modDiff.getSource());

			if (!nodeBefore.isLeaf()) {
				changeType(nodeBefore, subTreeBefore, DiffType.REM);
			}

//			GameEngine.showAndAwaitAll(Show.diff(subTreeBefore));

			parentAfter.insertChild(subTreeAfter.getRoot().getAllChildren().iterator().next(), indexAfter, Time.AFTER);
			parentBefore.insertChild(subTreeBefore.getRoot().getAllChildren().iterator().next(), indexBefore,
					Time.BEFORE);

//			GameEngine.showAndAwaitAll(Show.diff(modDiff));

		}
	}
	
	/**
	 * Patches the target variant with the changes from the source patch, requires also the full configurations of the source and the target variant.
	 * Applies also features which were added in the source patch if patchNewFeatures is set to true. Determines if a change should be applied to the
	 * target variant by checking if the change would also be present in the target variant (using the configurations of the variants).
	 * 
	 * @param sourcePatch The given patch on the source variant.
	 * @param targetVariant The given target variant.
	 * @param configSource The configuration of the source variant as {@link ConfigureWithFullConfig}
	 * @param configTarget The configuration of the target variant as {@link ConfigureWithFullConfig}
	 * @param debug Flag to print and show debug steps with GUI {@link GameEngine}.
	 * @param patchNewFeatures Flag whether features which were added in the source patch should be also applied to the target variant
	 * @return The patch as {@link VariationDiff} which includes all changes of the source patch that should be applied to the target variant.
	 * @throws Exception Throws an Exception if one or more changes could not be applied to the target variant.
	 */
	public static VariationDiff<DiffLinesLabel> patch(VariationDiff<DiffLinesLabel> sourcePatch,
			VariationTree<DiffLinesLabel> targetVariant, ConfigureWithFullConfig configSource, ConfigureWithFullConfig configTarget, boolean debug,
			boolean patchNewFeatures) throws Exception {

		VariationDiff<DiffLinesLabel> optimizedDiff = DiffView.optimized(sourcePatch, configTarget);

		if (debug) {
			GameEngine.showAndAwaitAll(Show.diff(optimizedDiff), Show.tree(optimizedDiff.project(Time.AFTER)));
		}

		VariationDiffSource source = optimizedDiff.getSource();
		VariationDiff<DiffLinesLabel> targetVariantDiffUnchanged = targetVariant.deepCopy()
				.toCompletelyUnchangedVariationDiff();
		VariationDiff<DiffLinesLabel> targetVariantDiffPatched = targetVariant.deepCopy()
				.toCompletelyUnchangedVariationDiff();

		Set<DiffNode<DiffLinesLabel>> removedNodes = new HashSet<DiffNode<DiffLinesLabel>>();
		Set<DiffNode<DiffLinesLabel>> addedNodes = new HashSet<DiffNode<DiffLinesLabel>>();

		// resolve nodes with two parents to two nodes, one added, one removed
		VariationDiff<DiffLinesLabel> diffCopy = optimizedDiff.deepCopy();
		List<DiffNode<DiffLinesLabel>> nodesToResolve = new ArrayList<>();

		optimizedDiff.forAll(node -> {
			if (node.isNon() && node.getParent(Time.BEFORE) != node.getParent(Time.AFTER)) {
				DiffNode<DiffLinesLabel> matchingNode = diffCopy.getNodeWithID(node.getID());
				nodesToResolve.add(matchingNode);
			}
		});

		resolve(optimizedDiff.getRoot(), diffCopy);

		if (debug) {
			GameEngine.showAndAwaitAll(Show.diff(diffCopy, "resolved"));
		}
		optimizedDiff = diffCopy;

		// remove old nodes
		optimizedDiff.forAll(node -> {
			if (node.isRem()) {
				removedNodes.add(node);
			}
		});
		Set<DiffNode<DiffLinesLabel>> removedSubtreeRoots = findRootsOfSubtrees(removedNodes, DiffType.REM, debug);
		List<DiffNode<DiffLinesLabel>> removedSortedSubtreeRoots = removedSubtreeRoots.stream()
				.sorted((n1, n2) -> Integer.compare(n1.getLinesAtTime(Time.BEFORE).fromInclusive(),
						n2.getLinesAtTime(Time.BEFORE).fromInclusive()))
				.collect(Collectors.toList());
		applyChanges(DiffType.REM, targetVariantDiffUnchanged, targetVariantDiffPatched, removedSortedSubtreeRoots,
				source, configSource, debug);

		// add new nodes
		optimizedDiff.forAll(node -> {
			if (node.isAdd()) {
				addedNodes.add(node);
			}
		});
		Set<DiffNode<DiffLinesLabel>> addedSubtreeRoots = findRootsOfSubtrees(addedNodes, DiffType.ADD, debug);
		List<DiffNode<DiffLinesLabel>> addedSortedSubtreeRoots = addedSubtreeRoots.stream().sorted((n1, n2) -> Integer
				.compare(n1.getLinesAtTime(Time.AFTER).fromInclusive(), n2.getLinesAtTime(Time.AFTER).fromInclusive()))
				.collect(Collectors.toList());
		applyChanges(DiffType.ADD, targetVariantDiffUnchanged, targetVariantDiffPatched, addedSortedSubtreeRoots,
				source, configSource, debug);

		if (debug) {
			GameEngine.showAndAwaitAll(Show.diff(sourcePatch), Show.tree(targetVariant), Show.diff(optimizedDiff),
					Show.diff(targetVariantDiffPatched), Show.tree(targetVariantDiffPatched.project(Time.AFTER)));
		}

		if (debug) {
			VariationDiff<DiffLinesLabel> targetVariantDiffPatchedCopy = targetVariantDiffPatched.deepCopy();
			VariationDiff<DiffLinesLabel> optimizedDiffCopy = optimizedDiff.deepCopy();
			CutNonEditedSubtrees.genericTransform(targetVariantDiffPatchedCopy);
			CutNonEditedSubtrees.genericTransform(optimizedDiffCopy);
			GameEngine.showAndAwaitAll(Show.diff(optimizedDiffCopy), Show.diff(targetVariantDiffPatchedCopy));
		}
		return targetVariantDiffPatched;
	}

}
