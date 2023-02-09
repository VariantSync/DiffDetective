package org.variantsync.diffdetective.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;

public class PatchAnalysis implements Analysis.Hooks {
    public static final String PATCH_STATISTICS_EXTENSION = ".patchStatistics.csv";

    private List<PatchStatistics> patchStatistics;
    private PatchStatistics thisPatchesStatistics;

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(EditClassCount.KEY, new EditClassCount());
    }

    @Override
    public void beginBatch(Analysis analysis) {
        patchStatistics = new ArrayList<>(Analysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
    }

    @Override
    public boolean beginPatch(Analysis analysis) {
        thisPatchesStatistics = new PatchStatistics(analysis.getPatch(), ProposedEditClasses.Instance);
        return true;
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
        analysis.getDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                final EditClass editClass = ProposedEditClasses.Instance.match(node);
                analysis.get(EditClassCount.KEY).reportOccurrenceFor(
                        editClass,
                        analysis.getCommitDiff()
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
    public void endBatch(Analysis analysis) {
        exportPatchStatistics(patchStatistics, FileUtils.addExtension(analysis.getOutputFile(), PATCH_STATISTICS_EXTENSION));
    }

    public static void exportPatchStatistics(final List<PatchStatistics> commitTimes, final Path pathToOutputFile) {
        final String csv = CSV.toCSV(commitTimes);

        try {
            IO.write(pathToOutputFile, csv);
        } catch (IOException e) {
            Logger.error(e);
            System.exit(1);
        }
    }
}
