import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.DiffFilter;
import diff.GitDiffer;
import diff.CommitDiff;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.transform.CollapseNonEditedSubtrees;
import load.GitLoader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import diff.serialize.DiffTreeSerializeDebugData;
import util.ExportUtils;
import diff.serialize.LineGraphExport;
import util.Yield;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DiffTreeMiner {
    private static void setupLogger(final Level loggingLevel) {
        Configurator configurator = Configurator.defaultConfig()
                .writer(new ConsoleWriter(), loggingLevel)
                .formatPattern("{{level}:|min-size=8} {message}");
        configurator.activate();
    }

    public static void main(String[] args) {
        setupLogger(Level.DEBUG);

        final Path outputPath = Paths.get("linegraph", "data", "difftrees.lg");

        final boolean fromZip = true;
        final String repo = "Marlin_old.zip";

        // use this option with large repositories
        // alternatively, increasing the java heap size also helps :D
        boolean saveMemory = true;
        boolean renderOutput = false;

        // The filter used by the GitDiffer
        final DiffFilter diffFilter = new DiffFilter.Builder()
                //.allowBinary(false)
                .allowMerge(false)
                .allowedPaths("Marlin.*")
                .blockedPaths(".*arduino.*")
                .allowedChangeTypes(DiffEntry.ChangeType.MODIFY)
                .allowedFileExtensions("c", "cpp", "h", "pde")
                .build();

        final LineGraphExport.Options exportOptions = new LineGraphExport.Options(
                LineGraphExport.NodePrintStyle.Type // For pattern matching, we want to look at node types and not individual code.
                , true
                , List.of(new CollapseNonEditedSubtrees())
        );

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
        int hardCap = 100;
        final DiffTreeSerializeDebugData debugData = new DiffTreeSerializeDebugData();
        Logger.info("Mining start");
        for (CommitDiff diff : yieldDiff) {
//            Logger.info("Exporting Commit #" + commitDiffCounter);
            final Pair<DiffTreeSerializeDebugData, Integer> res = LineGraphExport.toLineGraphFormat(diff, lineGraph, treeCounter, exportOptions);
            debugData.mappend(res.getKey());
            treeCounter = res.getValue();
//            ++commitDiffCounter;

//            if (hardCap > 0 && treeCounter >= hardCap) {
//                break;
//            }
        }

        Logger.info("Exported " + treeCounter + " diff trees!");
        Logger.info("Exported " + debugData.numExportedNonNodes + " nodes of diff type NON.");
        Logger.info("Exported " + debugData.numExportedAddNodes + " nodes of diff type ADD.");
        Logger.info("Exported " + debugData.numExportedRemNodes + " nodes of diff type REM.");

        try {
            Logger.info("Writing file " + outputPath);
            ExportUtils.write(outputPath, lineGraph.toString());
        } catch (IOException exception) {
            Logger.error(exception);
        }

        if (renderOutput) {
            Logger.info("Rendering " + outputPath);
            final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
            if (!renderer.renderFile(outputPath)) {
                Logger.error("Rendering " + outputPath + " failed!");
            }
        }

        Logger.info("Done");
    }
}
