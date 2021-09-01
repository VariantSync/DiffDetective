import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.DiffFilter;
import diff.GitDiffer;
import diff.data.*;
import load.GitLoader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import util.LineGraphExport;
import util.ExportUtils;
import util.DebugData;
import util.Yield;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DiffTreeMiner {
    private static void setupLogger(final Level loggingLevel) {
        Configurator configurator = Configurator.defaultConfig()
                .writer(new ConsoleWriter(), loggingLevel)
                .formatPattern("{{level}:|min-size=8} {message}");
        configurator.activate();
    }

    public static void main(String[] args) throws IOException {
        setupLogger(Level.DEBUG);

        final Path outputPath = Paths.get("difftrees.lg");

        final boolean fromZip = true;
        final String repo = "Marlin_old.zip";

        // use this option with large repositories
        // alternatively, increasing the java heap size also helps :D
        boolean saveMemory = true;

        // The filter used by the GitDiffer
        final DiffFilter diffFilter = new DiffFilter.Builder()
                //.allowBinary(false)
                .allowMerge(false)
                .allowedPaths("Marlin.*")
                .blockedPaths(".*arduino.*")
                .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
                .allowedFileExtensions("c", "cpp", "h", "pde")
                .build();

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        Git git;
        if (fromZip) {
            Logger.info("Loading git from {} ...", repo);
            git = GitLoader.fromZip(repo);
        } else {
            Logger.info("Loading git from {} ...", repo);
            git = GitLoader.fromDefaultDirectory(repo);
        }

        if (git == null) {
            Logger.error("Failed to load git");
            return;
        }

        // create GitDiff
        final GitDiffer differ = new GitDiffer(git, diffFilter, saveMemory);
        final Yield<CommitDiff> yieldDiff = differ.yieldGitDiff();

        final StringBuilder lineGraph = new StringBuilder();
        int treeCounter = 0;
        final DebugData debugData = new DebugData();
//        int commitDiffCounter = 1;
        for (CommitDiff diff : yieldDiff) {
//            Logger.info("Exporting CommitDiff #" + commitDiffCounter);
//            ++commitDiffCounter;
            final Pair<DebugData, Integer> res = LineGraphExport.toLineGraphFormat(diff, lineGraph, treeCounter);
            debugData.mappend(res.getKey());
            treeCounter = res.getValue();
        }

        Logger.info("Exported " + treeCounter + " diff trees!");
        Logger.info("Exported " + debugData.numExportedNonNodes + " nodes of diff type NON.");
        Logger.info("Exported " + debugData.numExportedAddNodes + " nodes of diff type ADD.");
        Logger.info("Exported " + debugData.numExportedRemNodes + " nodes of diff type REM.");

        try {
            ExportUtils.write(outputPath, lineGraph.toString());
        } catch (IOException exception) {
            Logger.error(exception);
        }
    }
}
