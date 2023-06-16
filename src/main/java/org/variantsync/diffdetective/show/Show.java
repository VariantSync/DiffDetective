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
    
    public static GameEngine diff(final DiffTree d, final String title) {
        return new GameEngine(new DiffTreeApp(
                title,
                d,
                DEFAULT_RESOLUTION
        ));
    }

    public static GameEngine diff(final DiffTree d) {
        return diff(d, d.getSource().toString());
    }

    public static GameEngine tree(final VariationTree t, final String title) {
        return new GameEngine(new DiffTreeApp(
                title,
                t.toCompletelyUnchangedDiffTree(),
                DEFAULT_RESOLUTION
        ));
    }

    public static GameEngine tree(final VariationTree t) {
        return tree(t, t.source().toString());
    }

    public static GameEngine baddiff(final BadVDiff badVDiff, final String title) {
        final DiffTree d = badVDiff.diff().toDiffTree(
            v -> {
                int from = v.getLineRange().fromInclusive();
                int to = v.getLineRange().toExclusive();

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
                title,
                d,
                DEFAULT_RESOLUTION
        ));
    }

    public static GameEngine baddiff(final BadVDiff badVDiff) {
        return baddiff(badVDiff, badVDiff.diff().source().toString());
    }
}
