package org.variantsync.diffdetective.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;

public class PatchAnalysis implements HistoryAnalysis.Hooks {
    public static final String PATCH_STATISTICS_EXTENSION = ".patchStatistics.csv";

    private List<PatchStatistics> patchStatistics;
    private PatchStatistics thisPatchesStatistics;

    @Override
    public void beginBatch(HistoryAnalysis analysis) {
        patchStatistics = new ArrayList<>(HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
    }

    @Override
    public boolean beginPatch(HistoryAnalysis analysis) {
        thisPatchesStatistics = new PatchStatistics(analysis.getCurrentPatch(), ProposedEditClasses.Instance);
        return true;
    }

    @Override
    public boolean analyzeDiffTree(HistoryAnalysis analysis) {
        analysis.getCurrentDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                final EditClass editClass = ProposedEditClasses.Instance.match(node);
                analysis.getResult().editClassCounts.reportOccurrenceFor(
                        editClass,
                        analysis.getCurrentCommitDiff()
                );
                thisPatchesStatistics.editClassCount().increment(editClass);
            }
        });

        return true;
    }

    @Override
    public void endPatch(HistoryAnalysis analysis) {
        patchStatistics.add(thisPatchesStatistics);
    }

    @Override
    public void endBatch(HistoryAnalysis analysis) throws IOException {
        exportPatchStatistics(patchStatistics, FileUtils.addExtension(analysis.getOutputFile(), PATCH_STATISTICS_EXTENSION));
    }

    public static void exportPatchStatistics(final List<PatchStatistics> commitTimes, final Path pathToOutputFile) throws IOException {
        IO.write(pathToOutputFile, CSV.toCSV(commitTimes));
    }
}
