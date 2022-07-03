package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

/**
 * Our RemFromPC pattern from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class RemFromPC extends ElementaryPattern {
    RemFromPC() {
        super("RemFromPC", DiffType.REM);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return !codeNode.getBeforeParent().isRem();
    }
}
