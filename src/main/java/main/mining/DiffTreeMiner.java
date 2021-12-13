package main.mining;

import datasets.DebugOptions;
import datasets.DefaultRepositories;
import datasets.Repository;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.difftree.CodeType;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.DiffTreeSerializeDebugData;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.LineGraphExport;
import diff.difftree.serialize.nodeformat.MiningDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.difftree.transform.*;
import main.Main;
import main.mining.strategies.CompositeDiffTreeMiningStrategy;
import main.mining.strategies.MineAndExportIncrementally;
import main.mining.strategies.MiningMonitor;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import util.IO;
import util.Yield;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DiffTreeMiner {
    public static final List<DiffTreeTransformer> PostProcessing = List.of(
//            new NaiveMovedCodeDetection(), // do this first as it might introduce non-edited subtrees
            new CutNonEditedSubtrees(),
//            RunningExampleFinder.Default,
            new CollapseNestedNonEditedMacros(),
            new CollapseAtomicPatterns(ProposedAtomicPatterns.Instance),
            new RelabelRoot(CodeType.IF.name)
    );

    private final static int DEBUG_treesToExportAtMost = -1;

    public static DiffTreeMiningResult mine(
            final Repository repo,
            final Path outputPath,
            final DiffTreeLineGraphExportOptions exportOptions,
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

        strategy.end();
        return new DiffTreeMiningResult(commitCounter, treeCounter, debugData);
    }

    public static void main(String[] args) {
        Main.setupLogger(Level.DEBUG);

        final boolean renderOutput = false;
        final DebugOptions debugOptions = new DebugOptions(DebugOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF);

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path linuxDir = Paths.get("..", "variantevolution_datasets");
        final Path outputDir = Paths.get("results", "mining");

        final List<Repository> repos = List.of(
//                DefaultRepositories.stanciulescuMarlinZip(Path.of("."))
                DefaultRepositories.createRemoteLinuxRepo(linuxDir.resolve("linux"))
//                DefaultRepositories.createRemoteVimRepo(inputDir.resolve("vim"))
        );

        final DiffTreeLineGraphExportOptions exportOptions = new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE
                , new CommitDiffDiffTreeLabelFormat()
                , new MiningDiffNodeFormat()
                , true
                , PostProcessing
                , DiffTreeLineGraphExportOptions.LogError()
                .andThen(DiffTreeLineGraphExportOptions.RenderError())
                .andThen(DiffTreeLineGraphExportOptions.SysExitOnError())
        );

        final DiffTreeMiningStrategy miningStrategy =
//                new MineAndExportIncrementally();
                new CompositeDiffTreeMiningStrategy(
                        new MineAndExportIncrementally(1000),
                        new MiningMonitor(10)
                );

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        for (final Repository repo : repos) {
            Logger.info(" === Begin Processing " + repo.getRepositoryName() + " ===");
            repo.setDebugOptions(debugOptions);
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
