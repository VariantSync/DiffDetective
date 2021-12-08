package diff.difftree.serialize.treeformat;

import diff.difftree.DiffTreeSource;

public class IndexedTreeFormat implements DiffTreeLabelFormat {
    private int nextId = 0;

    public IndexedTreeFormat() {
        reset();
    }

    public void reset() {
        nextId = 0;
    }

    @Override
    public DiffTreeSource fromLabel(String label) {
        return DiffTreeSource.Unknown;
    }

    @Override
    public String toLabel(DiffTreeSource diffTreeSource) {
        final String result = "" + nextId;
        ++nextId;
        return result;
    }
}
