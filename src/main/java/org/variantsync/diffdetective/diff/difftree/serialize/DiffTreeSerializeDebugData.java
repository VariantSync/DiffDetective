package org.variantsync.diffdetective.diff.difftree.serialize;

import org.variantsync.diffdetective.analysis.MetadataKeys;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.functjonal.category.InplaceSemigroup;

import java.util.LinkedHashMap;

/**
 * Debug data that keeps track of the number of exported nodes and their diffTypes.
 * @author Paul Bittner
 */
public class DiffTreeSerializeDebugData implements Metadata<DiffTreeSerializeDebugData> {
    /**
     * Inplace semigroup that sums all counts and writes them to the first given debug data.
     */
    public static final InplaceSemigroup<DiffTreeSerializeDebugData> ISEMIGROUP = (a, b) -> {
        a.numExportedNonNodes += b.numExportedNonNodes;
        a.numExportedAddNodes += b.numExportedAddNodes;
        a.numExportedRemNodes += b.numExportedRemNodes;
    };

    /**
     * Number of exported nodes with {@link org.variantsync.diffdetective.diff.difftree.DiffType#NON}.
     */
    public int numExportedNonNodes = 0;

    /**
     * Number of exported nodes with {@link org.variantsync.diffdetective.diff.difftree.DiffType#ADD}.
     */
    public int numExportedAddNodes = 0;

    /**
     * Number of exported nodes with {@link org.variantsync.diffdetective.diff.difftree.DiffType#REM}.
     */
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
