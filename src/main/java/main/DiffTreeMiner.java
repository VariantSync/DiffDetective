package main;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import datasets.Repository;
import datasets.LoadingParameter;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.transform.CollapseAtomicPatterns;
import diff.difftree.transform.CollapseNestedNonEditedMacros;
import diff.difftree.transform.CutNonEditedSubtrees;
import diff.difftree.transform.DiffTreeTransformer;
import diff.difftree.transform.NaiveMovedCodeDetection;
import diff.serialize.DiffTreeSerializeDebugData;
import diff.serialize.LineGraphExport;
import static diff.serialize.LineGraphExport.NodePrintStyle;
import static diff.serialize.LineGraphExport.Options;
import load.GitLoader;
import util.IO;
import util.Yield;


public class DiffTreeMiner {
    public static final List<DiffTreeTransformer> PostProcessing = List.of(
            new NaiveMovedCodeDetection(), // do this first as it might introduce non-edited subtrees
            new CutNonEditedSubtrees(),
            new CollapseNestedNonEditedMacros(),
            new CollapseAtomicPatterns()
    );

    public static void main(String[] args) {
        Main.setupLogger(Level.DEBUG);

        Repository repo = Repository.createLocalZipRepo("Marlin_old.zip");

        final LineGraphExport.Options exportOptions = new Options(
                NodePrintStyle.LabelOnly // For pattern matching, we want to look at node types and not individual code.
                , true
                , PostProcessing
                , Options.LogError()
                .andThen(Options.RenderError())
                .andThen(LineGraphExport.Options.SysExitError())
        );

        boolean renderOutput = true;
        
        int treesToExportAtMost = -1;

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

        Git git;
        if (repo.getLoad() == LoadingParameter.FROM_DIR) {
            Logger.info("Loading git from {} ...", repo.getRepositoryPath());
            git = GitLoader.fromZip(repo.getRepositoryPath());
        } else {
            Logger.info("Loading git from {} ...", repo.getRepositoryPath());
            git = GitLoader.fromDefaultDirectory(repo.getRepositoryPath());
        }

        if (git == null) {
            Logger.error("Failed to load git");
            return;
        }

        // create GitDiff
        final GitDiffer differ = new GitDiffer(git, Main.DefaultDiffFilterForMarlin, repo.isSaveMemory());
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
            Logger.info("Writing file " + repo.getOutputPath());
            IO.write(repo.getOutputPath(), lineGraph.toString());
        } catch (IOException exception) {
            Logger.error(exception);
        }

        if (renderOutput) {
            Logger.info("Rendering " + repo.getOutputPath());
            final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
            if (!renderer.renderFile(repo.getOutputPath())) {
                Logger.error("Rendering " + repo.getOutputPath() + " failed!");
            }
        }

        Logger.info("Done");
    }
}
