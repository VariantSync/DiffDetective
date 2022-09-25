package org.variantsync.diffdetective.analysis.strategies;

import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

/**
 * Composite pattern for AnalysisStrategy.
 * Composes multiple AnalysisStrategies as one.
 * @author Paul Bittner
 */
public class CompositeAnalysisStrategy extends AnalysisStrategy {
    private final Collection<AnalysisStrategy> strategies;

    /**
     * Creates a composite strategy for all given strategies.
     * The resulting strategy will forward any callbacks to the given strategies in the order they are given.
     * @param strategies Strategies to compose.
     */
    public CompositeAnalysisStrategy(final AnalysisStrategy... strategies) {
        this.strategies = Arrays.asList(strategies);
    }

    @Override
    public void start(Repository repo, Path outputPath) {
        super.start(repo, outputPath);
        for (final AnalysisStrategy s : strategies) {
            s.start(repo, outputPath);
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
