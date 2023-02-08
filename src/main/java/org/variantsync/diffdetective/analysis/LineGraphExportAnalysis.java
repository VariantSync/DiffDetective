package org.variantsync.diffdetective.analysis;

import java.io.OutputStream;

import org.variantsync.diffdetective.analysis.strategies.AnalysisStrategy;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExport;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExportOptions;

public class LineGraphExportAnalysis implements Analysis.Hooks<EditClassAnalysisResult> {
    private final AnalysisStrategy analysisStrategy;
    private final LineGraphExportOptions exportOptions;
    private OutputStream lineGraphDestination;

    public LineGraphExportAnalysis(final AnalysisStrategy analysisStrategy, final LineGraphExportOptions exportOptions) {
        this.analysisStrategy = analysisStrategy;
        this.exportOptions = exportOptions;
    }

    @Override
    public void beginBatch(Analysis<EditClassAnalysisResult> analysis) {
        analysis.getResult().putCustomInfo(MetadataKeys.TREEFORMAT, exportOptions.treeFormat().getName());
        analysis.getResult().putCustomInfo(MetadataKeys.NODEFORMAT, exportOptions.nodeFormat().getName());
        analysis.getResult().putCustomInfo(MetadataKeys.EDGEFORMAT, exportOptions.edgeFormat().getName());

        analysisStrategy.start(analysis.getRepository(), analysis.getOutputFile());
    }

    @Override
    public boolean onParsedCommit(Analysis<EditClassAnalysisResult> analysis) {
        lineGraphDestination = analysisStrategy.onCommit(analysis.getCommitDiff());
        return true;
    }

    @Override
    public boolean analyzeDiffTree(Analysis<EditClassAnalysisResult> analysis) throws Exception {
        analysis.getResult().append(LineGraphExport.toLineGraphFormat(analysis.getResult().repoName, analysis.getPatch(), exportOptions, lineGraphDestination));
        return true;
    }

    @Override
    public void endCommit(Analysis<EditClassAnalysisResult> analysis) throws Exception {
        lineGraphDestination.close();
    }

    @Override
    public void endBatch(Analysis<EditClassAnalysisResult> analysis) {
        analysisStrategy.end();
    }
}
