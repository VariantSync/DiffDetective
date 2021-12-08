package main.mining;

import diff.difftree.serialize.DiffTreeSerializeDebugData;

public record DiffTreeMiningResult(String lineGraph, int numCommits, int numTrees, DiffTreeSerializeDebugData debugData) {
    @Override
    public String toString() {
        return "Exported " + numTrees() + " diff trees!\n"
                + "Exported " + numCommits() + " commits!\n"
                + "Exported " + debugData().numExportedNonNodes + " nodes of diff type NON.\n"
                + "Exported " + debugData().numExportedAddNodes + " nodes of diff type ADD.\n"
                + "Exported " + debugData().numExportedRemNodes + " nodes of diff type REM.";
    }
}
