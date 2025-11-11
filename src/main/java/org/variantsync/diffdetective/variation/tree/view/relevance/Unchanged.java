package org.variantsync.diffdetective.variation.tree.view.relevance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.prop4j.Node;
import org.variantsync.diffdetective.show.Show;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.patching.Patching;
import org.variantsync.diffdetective.variation.tree.VariationNode;

public class Unchanged implements Relevance {
	private final VariationDiff<DiffLinesLabel> diff;
	private final Time time;
	private final Configure configSource;
	private Map<String, List<DiffNode<DiffLinesLabel>>> lookUpMap = new HashMap<>();

	public Unchanged(VariationDiff<DiffLinesLabel> diff, Time time, Configure configSource) {
		this.diff = diff;
		this.time = time;
		this.configSource = configSource;
		VariationDiff<DiffLinesLabel> diffCopy = diff.deepCopy();
		Patching.resolve((DiffNode<DiffLinesLabel>) diff.getRoot(),
				(VariationDiff<DiffLinesLabel>) diffCopy);
		diffCopy.forAll(diffNode -> {
//			if (time == Time.AFTER ? !diffNode.isRem() : !diffNode.isAdd()) {
				String key = getIdentifierForNode(diffNode);
				if (!lookUpMap.containsKey(key)) {
					lookUpMap.put(key, new ArrayList<DiffNode<DiffLinesLabel>>());
				}
				lookUpMap.get(key).add(diffNode);
//			}
		});
	}

	private String getIdentifierForNode(DiffNode<DiffLinesLabel> diffNode) {
		String key = diffNode.getNodeType().name();
		Node formula = diffNode.getFormula();
		if (formula != null) {
			key += formula.toString();
		}
		for (String s : diffNode.getLabel().getLines()) {
			key += s;
		}
		for (String s : diffNode.getLabel().getTrailingLines()) {
			key += s;
		}
		Node pc = diffNode.isRem() ? diffNode.getPresenceCondition(Time.BEFORE) : diffNode.getPresenceCondition(Time.AFTER);
		if (pc != null) {
			key += pc;
		}

		// add label of both siblings to identifier if existing
		DiffNode<DiffLinesLabel> parent = diffNode.getParent(time);
		if (parent != null) {

			List<DiffNode<DiffLinesLabel>> siblings = parent.getChildOrder(time);
			int indexDiffNode = siblings.indexOf(diffNode);

			if (indexDiffNode - 1 >= 0) {
				List<String> beforeSibling = siblings.get(indexDiffNode - 1).getLabel().getLines();
				for (String s : beforeSibling) {
					key += s;
				}
			}
			if (indexDiffNode + 1 < siblings.size()) {
				List<String> afterSibling = siblings.get(indexDiffNode + 1).getLabel().getLines();
				for (String s : afterSibling) {
					key += s;
				}
			}

		}
		return key;
	}

	private String getIdentifierForNode(VariationNode<?, ?> node) {
		String key = node.getNodeType().name();
		Node formula = node.getFormula();
		if (formula != null) {
			key += formula.toString();
		}
		for (String s : node.getLabel().getLines()) {
			key += s;
		}
		for (String s : node.getLabel().getTrailingLines()) {
			key += s;
		}
		Node pc = node.getPresenceCondition();
		if (pc != null) {
			key += pc;
		}

		// add label of both siblings to identifier if existing
		VariationNode<?, ?> parent = node.getParent();
		if (parent != null) {
			List<?> siblings = node.getParent().getChildren();
			int indexNode = siblings.indexOf(node);

			int indexBefore = indexNode - 1;
			VariationNode<?, ?> beforeSibling = null;
			if (indexBefore >= 0) {
				beforeSibling = (VariationNode<?, ?>) siblings.get(indexBefore);
				while (!configSource.test(beforeSibling)) {
					indexBefore--;
					if (indexBefore < 0) {
						break;
					}
					beforeSibling = (VariationNode<?, ?>) siblings.get(indexBefore);
				}
				if (beforeSibling != null) {
					for (String s : beforeSibling.getLabel().getLines()) {
						key += s;
					}
				}
			}
			int indexAfter = indexNode + 1;
			VariationNode<?, ?> afterSibling = null;
			if (indexAfter < siblings.size()) {
				afterSibling = (VariationNode<?, ?>) siblings.get(indexAfter);
				while (!configSource.test(afterSibling)) {
					indexAfter++;
					if (indexAfter >= siblings.size()) {
						break;
					}
					afterSibling = (VariationNode<?, ?>) siblings.get(indexAfter);
				}
				if (afterSibling != null) {
					for (String s : afterSibling.getLabel().getLines()) {
						key += s;
					}
				}
			}
		}
		return key;
	}

	@Override
	public boolean test(VariationNode<?, ?> t) {
		if (!configSource.test(t)) {
			return true;
		}
		String identifier = getIdentifierForNode(t);
		List<DiffNode<DiffLinesLabel>> tInDiffMatches = lookUpMap.get(identifier);
		if (tInDiffMatches == null) {
			return true;
		}
		DiffNode<DiffLinesLabel> tInDiff = tInDiffMatches.get(0);
		if (tInDiffMatches.size() > 1) {
			Map<DiffNode<DiffLinesLabel>, Integer> map = new HashMap<>();
			for (DiffNode<DiffLinesLabel> diffNode : tInDiffMatches) {
				Integer lineNumberDiffNode = diffNode.getLinesAtTime(time).fromInclusive();
				map.put(diffNode, Math.abs(t.getLineRange().fromInclusive() - lineNumberDiffNode));
			}
			List<Entry<DiffNode<DiffLinesLabel>, Integer>> list = new ArrayList<>(map.entrySet());
			list.sort(Entry.comparingByValue());
			tInDiff = list.get(0).getKey();
		}
		return tInDiff.isNon() && tInDiff.beforePathEqualsAfterPath();
	}

	@Override
	public String getFunctionName() {
		return "unchanged";
	}

	@Override
	public String parametersToString() {
		return diff.toString();
	}
}
