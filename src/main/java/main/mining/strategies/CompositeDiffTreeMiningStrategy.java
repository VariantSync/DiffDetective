package main.mining.strategies;

import datasets.Repository;
import diff.CommitDiff;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import main.mining.DiffTreeMiningStrategy;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

public class CompositeDiffTreeMiningStrategy extends DiffTreeMiningStrategy {
    private final Collection<DiffTreeMiningStrategy> strategies;

    public CompositeDiffTreeMiningStrategy(final DiffTreeMiningStrategy... strategies) {
        this.strategies = Arrays.asList(strategies);
    }

    @Override
    public void start(Repository repo, Path outputPath, DiffTreeLineGraphExportOptions options) {
        super.start(repo, outputPath, options);
        for (final DiffTreeMiningStrategy s : strategies) {
            s.start(repo, outputPath, options);
        }
    }

    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {
        for (final DiffTreeMiningStrategy s : strategies) {
            s.onCommit(commit, lineGraph);
        }
    }

    @Override
    public void end() {
        for (final DiffTreeMiningStrategy s : strategies) {
            s.end();
        }
    }
}
