package org.variantsync.diffdetective.show;

import java.util.List;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.show.engine.GameEngine;
import org.variantsync.diffdetective.show.engine.geom.Vec2;
import org.variantsync.diffdetective.show.variation.VariationDiffApp;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;
import org.variantsync.diffdetective.variation.tree.VariationTree;

public class Show {
    public static Vec2 DEFAULT_RESOLUTION = new Vec2(800, 600);

    public static <L extends Label> GameEngine diff(final VariationDiff<L> d, final String title, List<DiffNodeLabelFormat<L>> availableFormats) {
        return new GameEngine(new VariationDiffApp<>(
                title,
                d,
                DEFAULT_RESOLUTION,
                availableFormats
        ));
    }

    public static GameEngine diff(final VariationDiff<?> d, final String title) {
        return diff(d, title, VariationDiffApp.DEFAULT_FORMATS());
    }

    public static GameEngine diff(final VariationDiff<?> d) {
        return diff(d, d.getSource().toString());
    }

    public static <L extends Label> GameEngine tree(final VariationTree<L> t, final String title, List<DiffNodeLabelFormat<L>> availableFormats) {
        return new GameEngine(new VariationDiffApp<>(
                title,
                t.toCompletelyUnchangedVariationDiff(),
                DEFAULT_RESOLUTION,
                availableFormats
        ));
    }

    public static GameEngine tree(final VariationTree<?> t, final String title) {
        return tree(t, title, VariationDiffApp.DEFAULT_FORMATS());
    }

    public static GameEngine tree(final VariationTree<?> t) {
        return tree(t, t.source().toString());
    }

    public static <L extends Label> GameEngine baddiff(final BadVDiff<L> badVDiff, final String title, List<DiffNodeLabelFormat<L>> availableFormats) {
        final VariationDiff<L> d = badVDiff.diff().toVariationDiff(
            v -> {
                int from = v.getLineRange().fromInclusive();
                int to = v.getLineRange().toExclusive();

                return new DiffNode<>(
                        badVDiff.coloring().get(v),
                        v.getNodeType(),
                        new DiffLineNumber(from, from, from),
                        new DiffLineNumber(to, to, to),
                        v.getFormula(),
                        v.getLabel()
                );
            }
        );

        return new GameEngine(new VariationDiffApp<>(
                title,
                d,
                DEFAULT_RESOLUTION,
                availableFormats
        ));
    }

    public static GameEngine baddiff(final BadVDiff<?> badVDiff, final String title) {
        return baddiff(badVDiff, title, VariationDiffApp.DEFAULT_FORMATS());
    }

    public static GameEngine baddiff(final BadVDiff<?> badVDiff) {
        return baddiff(badVDiff, badVDiff.diff().source().toString());
    }
}
