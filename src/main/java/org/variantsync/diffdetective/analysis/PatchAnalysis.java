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

public class PatchAnalysis implements Analysis.Hooks {
    public static final String PATCH_STATISTICS_EXTENSION = ".patchStatistics.csv";

    private List<PatchStatistics> patchStatistics;
    private PatchStatistics thisPatchesStatistics;

    @Override
    public void beginBatch(Analysis analysis) {
        patchStatistics = new ArrayList<>(Analysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
    }

    @Override
    public boolean beginPatch(Analysis analysis) {
        thisPatchesStatistics = new PatchStatistics(analysis.getCurrentPatch(), ProposedEditClasses.Instance);
        return true;
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
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
    public void endPatch(Analysis analysis) {
        patchStatistics.add(thisPatchesStatistics);
    }

    @Override
    public void endBatch(Analysis analysis) throws IOException {
        exportPatchStatistics(patchStatistics, FileUtils.addExtension(analysis.getOutputFile(), PATCH_STATISTICS_EXTENSION));
    }

    public static void exportPatchStatistics(final List<PatchStatistics> commitTimes, final Path pathToOutputFile) throws IOException {
        IO.write(pathToOutputFile, CSV.toCSV(commitTimes));
    }
}
