package org.variantsync.diffdetective.analysis;

import java.io.OutputStream;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.NotImplementedException;
import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExport;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExportOptions;
import org.variantsync.functjonal.category.InplaceSemigroup;

public class LineGraphExportAnalysis implements Analysis.Hooks {
    public static final ResultKey<Result> RESULT = new ResultKey<>("LineGraphExportAnalysis");
    public static final class Result implements Metadata<Result> {
        public String treeFormat;
        public String nodeFormat;
        public String edgeFormat;

        public static final InplaceSemigroup<Result> ISEMIGROUP = (a, b) -> {
            a.treeFormat = Metadata.mergeEqual(a.treeFormat, b.treeFormat);
            a.nodeFormat = Metadata.mergeEqual(a.nodeFormat, b.nodeFormat);
            a.edgeFormat = Metadata.mergeEqual(a.edgeFormat, b.edgeFormat);
        };

        @Override
        public InplaceSemigroup<Result> semigroup() {
            return ISEMIGROUP;
        }

        @Override
        public LinkedHashMap<String, Object> snapshot() {
            var snap = new LinkedHashMap<String, Object>();
            snap.put(MetadataKeys.TREEFORMAT, treeFormat);
            snap.put(MetadataKeys.NODEFORMAT, nodeFormat);
            snap.put(MetadataKeys.EDGEFORMAT, edgeFormat);
            return snap;
        }

        @Override
        public void setFromSnapshot(LinkedHashMap<String, String> snap) {
            throw new NotImplementedException();
        }
    }

    private final AnalysisStrategy analysisStrategy;
    private final LineGraphExportOptions<? super DiffLinesLabel> exportOptions;
    private OutputStream lineGraphDestination;

    public LineGraphExportAnalysis(final AnalysisStrategy analysisStrategy, final LineGraphExportOptions<? super DiffLinesLabel> exportOptions) {
        this.analysisStrategy = analysisStrategy;
        this.exportOptions = exportOptions;
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(RESULT, new Result());
    }

    @Override
    public void beginBatch(Analysis analysis) {
        analysis.get(RESULT).treeFormat = exportOptions.treeFormat().getIdentifier();
        analysis.get(RESULT).nodeFormat = exportOptions.nodeFormat().getIdentifier();
        analysis.get(RESULT).edgeFormat = exportOptions.edgeFormat().getIdentifier();

        analysisStrategy.start(analysis.getRepository(), analysis.getOutputFile());
    }

    @Override
    public boolean onParsedCommit(Analysis analysis) {
        lineGraphDestination = analysisStrategy.onCommit(analysis.getCurrentCommitDiff());
        return true;
    }

    @Override
    public boolean analyzeVariationDiff(Analysis analysis) throws Exception {
        // BUG? All linegraphs are aggregated in the result but actually, we want to export them to a separate file
        //      linegraphs should be aggregated somewhere else and then exported, for example, at the end of a thread's
        //      job.
        analysis.append(
            LineGraphExport.STATISTIC,
            LineGraphExport.toLineGraphFormat(analysis.getCurrentPatch(), exportOptions, lineGraphDestination)
        );
        return true;
    }

    @Override
    public void endCommit(Analysis analysis) throws Exception {
        lineGraphDestination.close();
    }

    @Override
    public void endBatch(Analysis analysis) {
        analysisStrategy.end();
    }
}
