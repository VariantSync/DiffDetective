package main.mining;

import diff.difftree.serialize.DiffTreeSerializeDebugData;
import org.pmw.tinylog.Logger;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;

public class DiffTreeMiningResult {
    public final static String EXTENSION = ".metadata.txt";

    public int exportedCommits;
    public int exportedTrees;
    public final DiffTreeSerializeDebugData debugData;

    public DiffTreeMiningResult() {
        this(0, 0, new DiffTreeSerializeDebugData());
    }

    public DiffTreeMiningResult(int exportedCommits, int exportedTrees, final DiffTreeSerializeDebugData debugData) {
        this.exportedCommits = exportedCommits;
        this.exportedTrees = exportedTrees;
        this.debugData = debugData;
    }

    void mappend(final DiffTreeMiningResult other) {
        exportedCommits += other.exportedCommits;
        exportedTrees += other.exportedTrees;
        debugData.mappend(other.debugData);
    }

    void exportTo(final Path file) {
        try {
            IO.write(file, this.toString());
        } catch (IOException e) {
            Logger.error(e);
            System.exit(0);
        }
    }

    @Override
    public String toString() {
        return    "exported trees: " + exportedTrees + "\n"
                + "exported commits: " + exportedCommits + "\n"
                + "Exported " + debugData.numExportedNonNodes + " nodes of diff type NON.\n"
                + "Exported " + debugData.numExportedAddNodes + " nodes of diff type ADD.\n"
                + "Exported " + debugData.numExportedRemNodes + " nodes of diff type REM.";
    }
}
