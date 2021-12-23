package diff.difftree.serialize;

import metadata.Metadata;

import java.util.LinkedHashMap;

public class DiffTreeSerializeDebugData implements Metadata<DiffTreeSerializeDebugData> {
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
    public LinkedHashMap<String, Integer> snapshot() {
        final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("#NON nodes", numExportedNonNodes);
        map.put("#ADD nodes", numExportedAddNodes);
        map.put("#REM nodes", numExportedRemNodes);
        return map;
    }
}