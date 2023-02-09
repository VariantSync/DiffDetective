package org.variantsync.diffdetective.analysis;

import java.io.OutputStream;
import java.util.LinkedHashMap;

import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.metadata.Metadata;
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
            throw new UnsupportedOperationException("TODO Not implemented yet");
        }
    }

    private final AnalysisStrategy analysisStrategy;
    private final LineGraphExportOptions exportOptions;
    private OutputStream lineGraphDestination;

    public LineGraphExportAnalysis(final AnalysisStrategy analysisStrategy, final LineGraphExportOptions exportOptions) {
        this.analysisStrategy = analysisStrategy;
        this.exportOptions = exportOptions;
    }

    @Override
    public void initializeResults(Analysis analysis) {
        analysis.append(RESULT, new Result());
    }

    @Override
    public void beginBatch(Analysis analysis) {
        analysis.get(RESULT).treeFormat = exportOptions.treeFormat().getName();
        analysis.get(RESULT).nodeFormat = exportOptions.nodeFormat().getName();
        analysis.get(RESULT).edgeFormat = exportOptions.edgeFormat().getName();

        analysisStrategy.start(analysis.getRepository(), analysis.getOutputFile());
    }

    @Override
    public boolean onParsedCommit(Analysis analysis) {
        lineGraphDestination = analysisStrategy.onCommit(analysis.getCommitDiff());
        return true;
    }

    @Override
    public boolean analyzeDiffTree(Analysis analysis) throws Exception {
        analysis.append(
            LineGraphExport.STATISTIC,
            LineGraphExport.toLineGraphFormat(analysis.getPatch(), exportOptions, lineGraphDestination)
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
