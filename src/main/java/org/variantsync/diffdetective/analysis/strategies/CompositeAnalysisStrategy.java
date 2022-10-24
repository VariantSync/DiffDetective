package org.variantsync.diffdetective.analysis.strategies;

import org.apache.commons.io.output.TeeOutputStream;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;

import java.io.OutputStream;
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
    public OutputStream onCommit(CommitDiff commit) {
        var it = strategies.iterator();

        if (!it.hasNext()) {
            return OutputStream.nullOutputStream();
        }

        OutputStream destination = it.next().onCommit(commit);
        while (it.hasNext()) {
            destination = new TeeOutputStream(destination, it.next().onCommit(commit));
        }

        return destination;
    }

    @Override
    public void end() {
        for (final AnalysisStrategy s : strategies) {
            s.end();
        }
    }
}
