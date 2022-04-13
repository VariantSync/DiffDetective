package org.variantsync.diffdetective.analysis.strategies;

import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

public class CompositeAnalysisStrategy extends AnalysisStrategy {
    private final Collection<AnalysisStrategy> strategies;

    public CompositeAnalysisStrategy(final AnalysisStrategy... strategies) {
        this.strategies = Arrays.asList(strategies);
    }

    @Override
    public void start(Repository repo, Path outputPath, DiffTreeLineGraphExportOptions options) {
        super.start(repo, outputPath, options);
        for (final AnalysisStrategy s : strategies) {
            s.start(repo, outputPath, options);
        }
    }

    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {
        for (final AnalysisStrategy s : strategies) {
            s.onCommit(commit, lineGraph);
        }
    }

    @Override
    public void end() {
        for (final AnalysisStrategy s : strategies) {
            s.end();
        }
    }
}
