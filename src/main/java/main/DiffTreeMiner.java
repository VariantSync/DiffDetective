package main;

import datasets.DefaultRepositories;
import datasets.Repository;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.util.Pair;
import diff.CommitDiff;
import diff.GitDiffer;
import diff.difftree.CodeType;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.serialize.DiffTreeNodeLabelFormat;
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

import static diff.serialize.LineGraphExport.Options;


public class DiffTreeMiner {
    public static final List<DiffTreeTransformer> PostProcessing = List.of(
//            new NaiveMovedCodeDetection(), // do this first as it might introduce non-edited subtrees
            new CutNonEditedSubtrees(),
            new CollapseNestedNonEditedMacros(),
            new CollapseAtomicPatterns(),
            new RelabelRoot(CodeType.IF.name)
    );

    public static void main(String[] args) {
        Main.setupLogger(Level.DEBUG);

        final Repository repo;
		repo = DefaultRepositories.stanciulescuMarlinZip(Path.of("."));
//        repo = Repository.createRemoteLinuxRepo();
//        repo = Repository.createRemoteVimRepo();
        
        final Path outputPath = Paths.get("linegraph", "data", "difftrees.lg");
        
        final LineGraphExport.Options exportOptions = new Options(
                DiffTreeNodeLabelFormat.NodePrintStyle.Mining
                , true
                , PostProcessing
                , Options.LogError()
                .andThen(Options.RenderError())
                .andThen(LineGraphExport.Options.SysExitOnError())
        );

        boolean renderOutput = false;
        int treesToExportAtMost = -1;

        /* ************************ *\
        |      END OF ARGUMENTS      |
        \* ************************ */

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
