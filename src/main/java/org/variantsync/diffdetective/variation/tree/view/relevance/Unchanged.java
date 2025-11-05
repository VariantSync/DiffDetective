package org.variantsync.diffdetective.variation.tree.view.relevance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.prop4j.Node;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.tree.VariationNode;

public class Unchanged implements Relevance {
	private final VariationDiff<DiffLinesLabel> diff;
	private final Time time;
	private Map<String, List<DiffNode<DiffLinesLabel>>> lookUpMap = new HashMap<>();
	
	public Unchanged(VariationDiff<DiffLinesLabel> diff, Time time)  {
		this.diff = diff;
		this.time = time;
		diff.forAll(diffNode -> {
			if (time == Time.AFTER ? !diffNode.isRem() : !diffNode.isAdd()) {
				
				String key = getIdentifierForNode(diffNode);
				if (!lookUpMap.containsKey(key)) {
					lookUpMap.put(key, new ArrayList<DiffNode<DiffLinesLabel>>());
				}
				lookUpMap.get(key).add(diffNode);
			}
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
		Node pc = diffNode.getPresenceCondition(this.time);
		if (pc != null) {
			key += pc;
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
		return key;
	}

	@Override
	public boolean test(VariationNode<?, ?> t) {
		List<DiffNode<DiffLinesLabel>> tInDiffMatches = lookUpMap.get(getIdentifierForNode(t));
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
