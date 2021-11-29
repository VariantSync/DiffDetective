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
import main.mining.*;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import util.IO;
import util.Yield;

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

    public static DiffTreeMiningResult mine(
            final Repository repo,
            final Path outputPath,
            final LineGraphExport.Options exportOptions,
            final DiffTreeMiningStrategy strategy)
    {
        final GitDiffer differ = new GitDiffer(repo);
        final Yield<CommitDiff> yieldDiff = differ.yieldGitDiff();
        final DiffTreeSerializeDebugData debugData = new DiffTreeSerializeDebugData();

        int treeCounter = 0;
        int commitCounter = 0;

        Logger.info("Mining start");
        strategy.start(repo, outputPath, exportOptions);
        for (CommitDiff diff : yieldDiff) {
            final StringBuilder lineGraph = new StringBuilder();
            final Pair<DiffTreeSerializeDebugData, Integer> res = LineGraphExport.toLineGraphFormat(diff, lineGraph, treeCounter, exportOptions);
            debugData.mappend(res.getKey());

            treeCounter = res.getValue();
            ++commitCounter;

            strategy.onCommit(diff, lineGraph.toString());

            if (DEBUG_treesToExportAtMost > 0 && treeCounter >= DEBUG_treesToExportAtMost) {
                break;
            }
        }

        return new DiffTreeMiningResult(strategy.end(), commitCounter, treeCounter, debugData);
    }

    public static void main(String[] args) {
        Main.setupLogger(Level.DEBUG);

        boolean renderOutput = false;

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path linuxDir = Paths.get("..", "variantevolution_datasets");
        final Path outputDir = Paths.get("linegraph", "data");

        final List<Repository> repos = List.of(
//                DefaultRepositories.stanciulescuMarlinZip(Path.of(".")),
                DefaultRepositories.createRemoteLinuxRepo(linuxDir.resolve("linux"))
//                DefaultRepositories.createRemoteVimRepo(inputDir.resolve("vim"))
        );

        final LineGraphExport.Options exportOptions = new Options(
                NodePrintStyle.Mining
                , true
                , PostProcessing
                , Options.LogError()
                .andThen(Options.RenderError())
                .andThen(LineGraphExport.Options.SysExitOnError())
        );

        final DiffTreeMiningStrategy miningStrategy =
//                new MineAndExportIncrementally();
                new CompositeDiffTreeMiningStrategy(
                        new MineAndExportIncrementally(),
                        new MiningMonitor(5)
                );

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        for (final Repository repo : repos) {
            Logger.info(" === Begin Processing " + repo.getRepositoryName() + " ===");

            final Path lineGraphOutputPath = outputDir.resolve(repo.getRepositoryName() + ".lg");
            final Path metadataOutputPath = outputDir.resolve(repo.getRepositoryName() + ".metadata.txt");
            final DiffTreeMiningResult result = mine(repo, lineGraphOutputPath, exportOptions, miningStrategy);
            final String printedResult = result.toString();

            Logger.info("Writing the following metadata to " + metadataOutputPath + "\n" + printedResult);
            IO.tryWrite(metadataOutputPath, printedResult);

            if (renderOutput) {
                Logger.info("Rendering " + lineGraphOutputPath);
                final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
                if (!renderer.renderFile(lineGraphOutputPath)) {
                    Logger.error("Rendering " + lineGraphOutputPath + " failed!");
                }
            }

            Logger.info(" === End Processing " + repo.getRepositoryName() + " ===");
        }

        Logger.info("Done");
    }
}
