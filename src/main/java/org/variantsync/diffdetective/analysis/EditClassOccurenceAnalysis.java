package org.variantsync.diffdetective.analysis;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.StringUtils;

public class EditClassOccurenceAnalysis implements Analysis.Hooks {
    public static final String PATCH_STATISTICS_EXTENSION = ".patchStatistics.csv";

    private final AnalysisStrategy exportStrategy;
    private Writer output;

    public EditClassOccurenceAnalysis(AnalysisStrategy exportStrategy) {
        this.exportStrategy = exportStrategy;
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(EditClassCount.KEY, new EditClassCount(ProposedEditClasses.Instance));
    }

    @Override
    public void beginBatch(Analysis analysis) {
        exportStrategy.start(
            analysis.getRepository(),
            FileUtils.addExtension(analysis.getOutputFile(), PATCH_STATISTICS_EXTENSION)
        );
    }

    @Override
    public boolean beginCommit(Analysis analysis) {
        output = new OutputStreamWriter(exportStrategy.onCommit(analysis.getCurrentCommitDiff()));
        return true;
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws IOException {
        var editClassCounts = new LinkedHashMap<EditClass, Integer>();
        ProposedEditClasses.Instance.all().forEach(e -> editClassCounts.put(e, 0));

        analysis.getCurrentDiffTree().forAll(node -> {
            if (node.isArtifact()) {
                final EditClass editClass = ProposedEditClasses.Instance.match(node);

                analysis.get(EditClassCount.KEY).reportOccurrenceFor(
                        editClass,
                        analysis.getCurrentCommitDiff()
                );

                editClassCounts.computeIfPresent(editClass, (p, i) -> i + 1);
            }
        });

        output.write(
            Stream.concat(
                Stream.of(
                    analysis.getCurrentPatch().getCommitHash(),
                    analysis.getCurrentPatch().getFileName()
                ),
                editClassCounts.values().stream())
            .map(Object::toString)
            .collect(Collectors.joining(CSV.DEFAULT_CSV_DELIMITER))
        );
        output.write(StringUtils.LINEBREAK);

        return true;
    }

    @Override
    public void endCommit(Analysis analysis) throws IOException {
        output.close();
    }

    @Override
    public void endBatch(Analysis analysis) throws IOException {
        exportStrategy.end();
    }
}
