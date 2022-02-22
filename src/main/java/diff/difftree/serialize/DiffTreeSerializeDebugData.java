package diff.difftree.serialize;

import de.variantsync.functjonal.category.InplaceSemigroup;
import metadata.Metadata;

import java.util.LinkedHashMap;

public class DiffTreeSerializeDebugData implements Metadata<DiffTreeSerializeDebugData> {
    public final static String KEY_NON = "#NON nodes";
    public final static String KEY_ADD = "#ADD nodes";
    public final static String KEY_REM = "#REM nodes";

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
        map.put(KEY_NON, numExportedNonNodes);
        map.put(KEY_ADD, numExportedAddNodes);
        map.put(KEY_REM, numExportedRemNodes);
        return map;
    }

    @Override
    public InplaceSemigroup<DiffTreeSerializeDebugData> semigroup() {
        return ISEMIGROUP;
    }
}