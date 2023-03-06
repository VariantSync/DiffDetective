package org.variantsync.diffdetective.show;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.show.variation.DiffTreeApp;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.tree.VariationTree;

public class Show {
    /**
     * Todos
     * - menu for node format selection
     */
    
    public static void show(final DiffTree d) {
        int resx = 800;
        int resy = 600;

        new DiffTreeApp(d.getSource().toString(), d, resx, resy).run();
    }

    public static void show(final VariationTree t) {
        int resx = 800;
        int resy = 600;

        new DiffTreeApp(t.source().toString(), t.toCompletelyUnchangedDiffTree(), resx, resy).run();
    }

    public static void show(final BadVDiff badVDiff) {
        int resx = 800;
        int resy = 600;

        final DiffTree d = badVDiff.diff().toDiffTree(
            v -> {
                int from = v.getLineRange().getFromInclusive();
                int to = v.getLineRange().getToExclusive();

                return new DiffNode(
                        badVDiff.coloring().get(v),
                        v.getNodeType(),
                        new DiffLineNumber(from, from, from),
                        new DiffLineNumber(to, to, to),
                        v.getFormula(),
                        v.getLabelLines()
                );
            }
        );

//        new VariationTreeApp(t, resx, resy).run();
        new DiffTreeApp(badVDiff.diff().source().toString(), d, resx, resy).run();
    }
}
