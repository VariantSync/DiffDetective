package diff.difftree.serialize;

import de.variantsync.functjonal.category.InplaceSemigroup;
import metadata.Metadata;

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
        map.put("#NON nodes", numExportedNonNodes);
        map.put("#ADD nodes", numExportedAddNodes);
        map.put("#REM nodes", numExportedRemNodes);
        return map;
    }

    @Override
    public InplaceSemigroup<DiffTreeSerializeDebugData> semigroup() {
        return ISEMIGROUP;
    }
}