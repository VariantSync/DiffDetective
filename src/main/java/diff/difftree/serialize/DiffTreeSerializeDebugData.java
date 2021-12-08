package diff.difftree.serialize;

public class DiffTreeSerializeDebugData {
    public int numExportedNonNodes = 0;
    public int numExportedAddNodes = 0;
    public int numExportedRemNodes = 0;

    public void mappend(DiffTreeSerializeDebugData other) {
        numExportedNonNodes += other.numExportedNonNodes;
        numExportedAddNodes += other.numExportedAddNodes;
        numExportedRemNodes += other.numExportedRemNodes;
    }
}