package org.variantsync.diffdetective.variation.diff.patching;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;

public class Patching {
	public static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b, Time time) {
    	return isSameAs(a, b, new HashSet<>(), time);
    }
    
    private static <L extends Label> boolean isSameAs(DiffNode<L> a, DiffNode<L> b, Set<DiffNode<L>> visited, Time time) {
        if (!visited.add(a)) {
            return true;
        }

        if (!(
                a.getNodeType().equals(b.getNodeType()) &&
//                a.getFromLine().atTime(time) == (b.getFromLine().atTime(time)) &&
//                a.getToLine().atTime(time) == (b.getToLine().atTime(time)) &&
                (a.getFormula() == null ? b.getFormula() == null : a.getFormula().equals(b.getFormula())) &&
                a.getLabel().getLines().equals(b.getLabel().getLines())
        )) {
            return false;
        }

        Iterator<DiffNode<L>> aIt = a.getAllChildren().iterator();
        Iterator<DiffNode<L>> bIt = b.getAllChildren().iterator();
        while (aIt.hasNext() && bIt.hasNext()) {
            if (!isSameAs(aIt.next(), bIt.next(), visited, time)) {
                return false;
            }
        }

        return aIt.hasNext() == bIt.hasNext();
    }
}
