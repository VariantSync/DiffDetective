package org.variantsync.diffdetective.editclass;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;

/**
 * Abstract edit class according to our ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
public abstract class EditClass extends Pattern<DiffNode> {
    private final DiffType diffType;

    /**
     * Each edit class handles exactly one DiffType.
     * @param name unique identifier (see {@link Pattern}).
     * @param diffType This edit class matches only {@link DiffNode}s of the given {@link DiffType}.
     */
    public EditClass(final String name, final DiffType diffType) {
        super(name);
        this.diffType = diffType;
    }

    /**
     * Returns the diff type nodes matched by this edit class.
     */
    public DiffType getDiffType() {
        return diffType;
    }

    /**
     * Returns true iff the given node matches this edit class.
     * @param artifactNode Node which has node type ARTIFACT and whose DiffType is the same as {@link getDiffType()}.
     */
    protected abstract boolean matchesArtifactNode(DiffNode artifactNode);

    /**
     * Returns true if this edit class matches the given node and is an artifact.
     */
    @Override
    public final boolean matches(DiffNode node) {
        return node.isArtifact() && node.diffType == diffType && matchesArtifactNode(node);
    }

    /**
     * Returns true iff this edit class matches at leat one node on the given tree.
     */
    public boolean anyMatch(final DiffTree t) {
        return t.anyMatch(this::matches);
    }
}
