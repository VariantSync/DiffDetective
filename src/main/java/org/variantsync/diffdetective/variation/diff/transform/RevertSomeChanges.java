package org.variantsync.diffdetective.variation.diff.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

/**
 * This transformer reverts some changes in a variation diff.
 * The transformation requires a predicate on {@link DiffNode nodes} to identify the changes which should be undone.
 * An inserted node will be removed, so that the insertion does not happen.
 * A removed node will be made unchanged so that it will not be deleted.
 * When a node is made unchanged, also its parent node will be made unchanged if necessary to retain diff consistency.
 * @see DiffNode#makeUnchanged()
 * @author Paul Bittner
 */
public class RevertSomeChanges<L extends Label> implements Transformer<VariationDiff<L>> {
    private Predicate<DiffNode<L>> isInteresting;

    public RevertSomeChanges(final Predicate<DiffNode<L>> isInteresting) {
        Assert.assertNotNull(isInteresting);
        this.isInteresting = isInteresting;
    }

    @Override
    public void transform(VariationDiff<L> d) {
        final List<DiffNode<L>> nodesToEliminate = new ArrayList<>();
        final List<DiffNode<L>> toUnchanged = new ArrayList<>();
        d.forAll((DiffNode<L> node) -> {
                if (isInteresting.test(node)) {
                    if (node.isAdd()) {
                        nodesToEliminate.add(node);
                    } else if (node.isRem()) {
                        toUnchanged.add(node);
                    }
                }
            });

        for (DiffNode<L> zombie : nodesToEliminate) {
            // We also drop all children.
            zombie.drop();
        }

        for (DiffNode<L> n : toUnchanged) {
            n.makeUnchanged();
        }
    }
}
