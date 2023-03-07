package org.variantsync.diffdetective.show;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.variation.DiffTreeApp;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.tree.VariationTree;

public class Show {
    public static Vec2 DEFAULT_RESOLUTION = new Vec2(800, 600);
    
    public static GameEngine diff(final DiffTree d) {
        return new GameEngine(new DiffTreeApp(
                d.getSource().toString(),
                d,
                DEFAULT_RESOLUTION
        ));
    }

    public static GameEngine tree(final VariationTree t) {
        return new GameEngine(new DiffTreeApp(
                t.source().toString(),
                t.toCompletelyUnchangedDiffTree(),
                DEFAULT_RESOLUTION
        ));
    }

    public static GameEngine baddiff(final BadVDiff badVDiff) {
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

        return new GameEngine(new DiffTreeApp(
                badVDiff.diff().source().toString(),
                d,
                DEFAULT_RESOLUTION
        ));
    }
}
