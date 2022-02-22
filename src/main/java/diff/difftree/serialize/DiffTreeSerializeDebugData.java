package diff.difftree.serialize;

import de.variantsync.functjonal.category.InplaceSemigroup;
import metadata.Metadata;
import mining.MetadataKeys;

import java.util.LinkedHashMap;

public class DiffTreeSerializeDebugData implements Metadata<DiffTreeSerializeDebugData> {

    public static final InplaceSemigroup<DiffTreeSerializeDebugData> ISEMIGROUP = (a, b) -> {
        a.numExportedNonNodes += b.numExportedNonNodes;
        a.numExportedAddNodes += b.numExportedAddNodes;
        a.numExportedRemNodes += b.numExportedRemNodes;
    };

    public int numExportedNonNodes = 0;
    public int numExportedAddNodes = 0;
    public int numExportedRemNodes = 0;

    @Override
    public LinkedHashMap<String, Integer> snapshot() {
        final LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put(MetadataKeys.NON_NODE_COUNT, numExportedNonNodes);
        map.put(MetadataKeys.ADD_NODE_COUNT, numExportedAddNodes);
        map.put(MetadataKeys.REM_NODE_COUNT, numExportedRemNodes);
        return map;
    }

    @Override
    public InplaceSemigroup<DiffTreeSerializeDebugData> semigroup() {
        return ISEMIGROUP;
    }
}