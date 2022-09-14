package org.variantsync.diffdetective.pattern.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.ElementaryPattern;

/**
 * Our RemWithMapping pattern from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class RemWithMapping extends ElementaryPattern {
    RemWithMapping() {
        super("RemWithMapping", DiffType.REM);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode artifactNode) {
        return artifactNode.getBeforeParent().isRem();
    }
}
