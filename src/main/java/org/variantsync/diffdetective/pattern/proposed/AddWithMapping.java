package org.variantsync.diffdetective.pattern.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.ElementaryPattern;

/**
 * Our AddWithMapping pattern from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class AddWithMapping extends ElementaryPattern {
    AddWithMapping() {
        super("AddWithMapping", DiffType.ADD);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode artifactNode) {
        return artifactNode.getAfterParent().isAdd();
    }
}
