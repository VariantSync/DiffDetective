package main;

import datasets.DefaultRepositories;
import datasets.Repository;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.difftree.CodeType;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.transform.*;
import diff.serialize.DiffTreeSerializeDebugData;
import diff.serialize.LineGraphExport;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import util.IO;
import util.Yield;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static diff.serialize.LineGraphExport.NodePrintStyle;
import static diff.serialize.LineGraphExport.Options;


public class DiffTreeMiner {
    public static final List<DiffTreeTransformer> PostProcessing = List.of(
//            new NaiveMovedCodeDetection(), // do this first as it might introduce non-edited subtrees
            new CutNonEditedSubtrees(),
            new CollapseNestedNonEditedMacros(),
            new CollapseAtomicPatterns(),
            new RelabelRoot(CodeType.IF.name)
    );

    private final static int DEBUG_treesToExportAtMost = -1;

    public static DiffTreeMiningResult mine(final Repository repo, final LineGraphExport.Options exportOptions) {
        final GitDiffer differ = new GitDiffer(repo);
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

            if (DEBUG_treesToExportAtMost > 0 && treeCounter >= DEBUG_treesToExportAtMost) {
                break;
            }
        }

        return new DiffTreeMiningResult(lineGraph.toString(), treeCounter, debugData);
    }

    public static void export(final Path outputPath, final String linegraph) {
        try {
            Logger.info("Writing file " + outputPath);
            IO.write(outputPath, linegraph);
        } catch (IOException exception) {
            Logger.error(exception);
        }
    }

    public static void main(String[] args) {
        Main.setupLogger(Level.DEBUG);

        boolean renderOutput = false;
        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path outputDir = Paths.get("linegraph", "data");
        final LineGraphExport.Options exportOptions = new Options(
                NodePrintStyle.Mining
                , true
                , PostProcessing
                , Options.LogError()
                .andThen(Options.RenderError())
                .andThen(LineGraphExport.Options.SysExitOnError())
        );

        final List<Repository> repos = List.of(
//                DefaultRepositories.stanciulescuMarlinZip(Path.of(".")),
                DefaultRepositories.createRemoteLinuxRepo(inputDir.resolve("linux")),
                DefaultRepositories.createRemoteVimRepo(inputDir.resolve("vim"))
        );

//        repo = Repository.createRemoteLinuxRepo();
//        repo = Repository.createRemoteVimRepo();

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        for (final Repository repo : repos) {
            Logger.info(" === Begin Processing " + repo.getRepositoryName() + " ===");

            final Path outputPath = outputDir.resolve(repo.getRepositoryName() + ".lg");
            final DiffTreeMiningResult result = mine(repo, exportOptions);

            Logger.info("Exported " + result.numTrees() + " diff trees!");
            Logger.info("Exported " + result.debugData().numExportedNonNodes + " nodes of diff type NON.");
            Logger.info("Exported " + result.debugData().numExportedAddNodes + " nodes of diff type ADD.");
            Logger.info("Exported " + result.debugData().numExportedRemNodes + " nodes of diff type REM.");

            export(outputPath, result.lineGraph());

            if (renderOutput) {
                Logger.info("Rendering " + outputPath);
                final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
                if (!renderer.renderFile(outputPath)) {
                    Logger.error("Rendering " + outputPath + " failed!");
                }
            }

            Logger.info(" === End Processing " + repo.getRepositoryName() + " ===");
        }

        Logger.info("Done");
    }
}
