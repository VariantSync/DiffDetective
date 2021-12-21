package diff.difftree.serialize;

import metadata.Metadata;
import util.Semigroup;

import java.util.Map;

public class DiffTreeSerializeDebugData implements Semigroup<DiffTreeSerializeDebugData>, Metadata {
    public int numExportedNonNodes = 0;
    public int numExportedAddNodes = 0;
    public int numExportedRemNodes = 0;

    @Override
    public void append(DiffTreeSerializeDebugData other) {
        numExportedNonNodes += other.numExportedNonNodes;
        numExportedAddNodes += other.numExportedAddNodes;
        numExportedRemNodes += other.numExportedRemNodes;
    }

    @Override
    public Map<String, Integer> snapshot() {
        return Map.of(
                "#NON nodes", numExportedNonNodes,
                "#ADD nodes", numExportedAddNodes,
                "#REM nodes", numExportedRemNodes
        );
    }
}