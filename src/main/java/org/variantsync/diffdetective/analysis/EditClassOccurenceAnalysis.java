package org.variantsync.diffdetective.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.EditClassCatalogue;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;

public class EditClassOccurenceAnalysis implements Analysis.Hooks {
    /**
     * Statistics for processing a patch in a commit.
     * @param patchDiff The diff of the processed patch.
     * @param editClassCount Count statistics for the edit class matched to the edits in the patch.
     * @author Paul Bittner, Benjamin Moosherr
     */
    private static record Counts(
            PatchDiff patchDiff,
            Map<EditClass, Integer> editClassCounts
    ) implements CSV {
        /**
         * Creates empty patch statistics for the given catalogue of edit classes.
         * @param patch The patch to gather statistics for.
         * @param catalogue A catalogue of edit classes which should be used for classifying edits.
         */
        public Counts(final PatchDiff patch, final EditClassCatalogue catalogue) {
            this(patch, new LinkedHashMap<>());
            catalogue.all().forEach(e -> editClassCounts.put(e, 0));
        }

        /**
         * Increment the count for the given edit class.
         * The given edit class is assumed to be part of this counts catalog.
         * @param editClass The edit class whose count to increase by one.
         * @see Counts(PatchDiff, EditClassCatalogue)
         */
        public void increment(final EditClass editClass) {
            editClassCounts.computeIfPresent(editClass, (p, i) -> i + 1);
        }

        @Override
        public String toCSV(final String delimiter) {
            var counts = editClassCounts
                .values()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
            return patchDiff.getCommitHash() + delimiter + patchDiff.getFileName() + delimiter + counts;
        }
    }

    public static final String PATCH_STATISTICS_EXTENSION = ".patchStatistics.csv";

    private List<Counts> patchStatistics;
    private Counts thisPatchesStatistics;

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
        thisPatchesStatistics = new Counts(analysis.getCurrentPatch(), ProposedEditClasses.Instance);
        return true;
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) {
        analysis.getCurrentDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                final EditClass editClass = ProposedEditClasses.Instance.match(node);
                analysis.get(EditClassCount.KEY).reportOccurrenceFor(
                        editClass,
                        analysis.getCurrentCommitDiff()
                );
                thisPatchesStatistics.increment(editClass);
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

    public static void exportPatchStatistics(final List<Counts> commitTimes, final Path pathToOutputFile) throws IOException {
        IO.write(pathToOutputFile, CSV.toCSV(commitTimes));
    }
}
