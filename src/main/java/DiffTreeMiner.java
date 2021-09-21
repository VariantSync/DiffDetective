import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.transform.CollapseNestedNonEditedMacros;
import diff.difftree.transform.CutNonEditedSubtrees;
import diff.difftree.transform.DiffTreeTransformer;
import diff.difftree.transform.NaiveMovedCodeDetection;
import diff.serialize.DiffTreeSerializeDebugData;
import diff.serialize.LineGraphExport;
import load.GitLoader;
import org.eclipse.jgit.api.Git;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import util.IO;
import util.Yield;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DiffTreeMiner {
    public static final List<DiffTreeTransformer> PostProcessing = List.of(
            new CutNonEditedSubtrees(),
            new CollapseNestedNonEditedMacros(),
            new CutNonEditedSubtrees(), // duplicate as we found that there can occur new non-edited subtrees
            new NaiveMovedCodeDetection()
    );

    public static void main(String[] args) {
        Main.setupLogger(Level.DEBUG);

        final Path outputPath = Paths.get("linegraph", "data", "difftrees.lg");

        final boolean fromZip = true;
        final String repo = "Marlin_old.zip";

        // use this option with large repositories
        // alternatively, increasing the java heap size also helps :D
        boolean saveMemory = true;
        boolean renderOutput = false;
//        int treesToExportAtMost = 100;
        int treesToExportAtMost = -1;

        final LineGraphExport.Options exportOptions = new LineGraphExport.Options(
                LineGraphExport.NodePrintStyle.Type // For pattern matching, we want to look at node types and not individual code.
                , true
                , PostProcessing
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
        final GitDiffer differ = new GitDiffer(git, Main.DefaultDiffFilterForMarlin, saveMemory);
        final Yield<CommitDiff> yieldDiff = differ.yieldGitDiff();

        final StringBuilder lineGraph = new StringBuilder();
        int treeCounter = 0;
        final DiffTreeSerializeDebugData debugData = new DiffTreeSerializeDebugData();
        Logger.info("Mining start");
        for (CommitDiff diff : yieldDiff) {
//            Logger.info("Exporting Commit #" + commitDiffCounter);
            final Pair<DiffTreeSerializeDebugData, Integer> res = LineGraphExport.toLineGraphFormat(diff, lineGraph, treeCounter, exportOptions);
            debugData.mappend(res.getKey());
            treeCounter = res.getValue();
//            ++commitDiffCounter;

            if (treesToExportAtMost > 0 && treeCounter >= treesToExportAtMost) {
                break;
            }
        }

        Logger.info("Exported " + treeCounter + " diff trees!");
        Logger.info("Exported " + debugData.numExportedNonNodes + " nodes of diff type NON.");
        Logger.info("Exported " + debugData.numExportedAddNodes + " nodes of diff type ADD.");
        Logger.info("Exported " + debugData.numExportedRemNodes + " nodes of diff type REM.");

        try {
            Logger.info("Writing file " + outputPath);
            IO.write(outputPath, lineGraph.toString());
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
