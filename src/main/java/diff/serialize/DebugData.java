package diff.serialize;

public class DebugData {
    public int numExportedNonNodes = 0;
    public int numExportedAddNodes = 0;
    public int numExportedRemNodes = 0;

    public void mappend(DebugData other) {
        numExportedNonNodes += other.numExportedNonNodes;
        numExportedAddNodes += other.numExportedAddNodes;
        numExportedRemNodes += other.numExportedRemNodes;
    }
}