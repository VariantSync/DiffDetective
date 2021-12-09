package main.mining;

import datasets.DebugOptions;
import datasets.DefaultRepositories;
import datasets.Repository;
import diff.GitDiffer;
import diff.difftree.CodeType;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.nodeformat.MiningDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.difftree.transform.*;
import main.Main;
import main.mining.strategies.MineAllThenExport;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import parallel.ScheduledTasksIterator;
import util.Clock;
import util.ClusteredIterator;
import util.Diagnostics;
import util.MappedIterator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class DiffTreeMiner {
    private final static Diagnostics DIAGNOSTICS = new Diagnostics();

    public static final int COMMITS_TO_PROCESS_PER_THREAD = 500;

    public static List<DiffTreeTransformer> Postprocessing() {
        return List.of(
//                new NaiveMovedCodeDetection(), // do this first as it might introduce non-edited subtrees
                new CutNonEditedSubtrees(),
//                RunningExampleFinder.Default,
                new CollapseNestedNonEditedMacros(),
                new CollapseAtomicPatterns(),
                new RelabelRoot(CodeType.IF.name)
        );
    }

    public static void mine(
            final Repository repo,
            final Path outputDir,
            final DiffTreeLineGraphExportOptions exportOptions,
            final DiffTreeMiningStrategy strategy)
    {
        DiffTreeMiningResult totalResult;
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        Logger.info(">>> Scheduling synchronous mining");
        clock.start();
        final MiningTask task = new MiningTask(
                repo,
                differ,
                outputDir.resolve(repo.getRepositoryName() + ".lg"),
                exportOptions,
                strategy,
                differ.yieldRevCommits().toList()
                );
        Logger.info("<<< done after " + clock.printPassedSeconds());

        Logger.info(">>> Run mining");
        clock.start();
        try {
            totalResult = task.call();
        } catch (Exception e) {
            Logger.error(e);
            return;
        }
        Logger.info("<<< done after " + clock.printPassedSeconds());

        totalResult.exportTo(outputDir.resolve("totalresult" + DiffTreeMiningResult.EXTENSION));
    }

    public static void mineAsync(
            final Repository repo,
            final Path outputDir,
            final Supplier<DiffTreeLineGraphExportOptions> exportOptions,
            final Supplier<DiffTreeMiningStrategy> strategyFactory)
    {
        final DiffTreeMiningResult totalResult = new DiffTreeMiningResult();
        final GitDiffer differ = new GitDiffer(repo);
        final Clock clock = new Clock();

        // prepare tasks
        final int nThreads = DIAGNOSTICS.getNumberOfAvailableProcessors();
        Logger.info(">>> Scheduling asynchronous mining on " + nThreads + " threads.");
        clock.start();
        final Iterator<MiningTask> tasks =
                new MappedIterator<>(
                        new ClusteredIterator<>(differ.yieldRevCommits(), COMMITS_TO_PROCESS_PER_THREAD),
                        commitList -> new MiningTask(
                            repo,
                            differ,
                            outputDir.resolve(commitList.get(0).getId().getName() + ".lg"),
                            exportOptions.get(),
                            strategyFactory.get(),
                            commitList)
                );

        Logger.info("<<< done in " + clock.printPassedSeconds());

        Logger.info(">>> Run mining");
        clock.start();
        try (final ScheduledTasksIterator<DiffTreeMiningResult> threads = new ScheduledTasksIterator<>(tasks, nThreads)) {
            while (threads.hasNext()) {
                totalResult.mappend(threads.next());
            }
        } catch (Exception e) {
            Logger.error("Failed to run all mining task!");
            Logger.error(e);
        }
        Logger.info("<<< done in " + clock.printPassedSeconds());

        totalResult.exportTo(outputDir.resolve("totalresult" + DiffTreeMiningResult.EXTENSION));
    }

    public static void main(String[] args) {
        Main.setupLogger(Level.INFO);

        final DebugOptions debugOptions = new DebugOptions(DebugOptions.DiffStoragePolicy.REMEMBER_STRIPPED_DIFF);

        final Path inputDir = Paths.get("..", "DiffDetectiveMining");
        final Path linuxDir = Paths.get("..", "variantevolution_datasets");
        final Path outputDir = Paths.get("results", "mining");

        final List<Repository> repos = List.of(
                DefaultRepositories.stanciulescuMarlinZip(Path.of("."))
//                DefaultRepositories.createRemoteLinuxRepo(linuxDir.resolve("linux"))
//                DefaultRepositories.createRemoteVimRepo(inputDir.resolve("vim"))
        );

        final Supplier<DiffTreeLineGraphExportOptions> exportOptions = () -> new DiffTreeLineGraphExportOptions(
                GraphFormat.DIFFTREE
                , new CommitDiffDiffTreeLabelFormat()
                , new MiningDiffNodeFormat()
                , true
                , Postprocessing()
                , DiffTreeLineGraphExportOptions.LogError()
                .andThen(DiffTreeLineGraphExportOptions.RenderError())
                .andThen(DiffTreeLineGraphExportOptions.SysExitOnError())
        );

        final Supplier<DiffTreeMiningStrategy> miningStrategy = MineAllThenExport::new;
//                new CompositeDiffTreeMiningStrategy(
//                        new MineAndExportIncrementally(1000),
//                        new MiningMonitor(10)
//                );

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        for (final Repository repo : repos) {
            Logger.info(" === Begin Processing " + repo.getRepositoryName() + " ===");
            final Clock clock = new Clock();
            clock.start();

            repo.setDebugOptions(debugOptions);
            final Path repoOutputDir = outputDir.resolve(repo.getRepositoryName());
            mineAsync(repo, repoOutputDir, exportOptions, miningStrategy);
//            mine(repo, repoOutputDir, exportOptions.get(), miningStrategy.get());

            Logger.info(" === End Processing " + repo.getRepositoryName() + " after " + clock.printPassedSeconds() + " ===");
        }

        Logger.info("Done");
    }
}
