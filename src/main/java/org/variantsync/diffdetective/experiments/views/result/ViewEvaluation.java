package org.variantsync.diffdetective.experiments.views.result;

import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

import static org.variantsync.functjonal.Functjonal.intercalate;

public record ViewEvaluation(
//        Repository repo,
        String commit,
        String file,
        Query query,
        long msNaive,
        long msOptimized
) implements CSV {
    public static String makeHeader(String delimiter) {
        return intercalate(delimiter,
//                "repository",
                "commit",
                "file",
                "jtype",
                "jquery",
                "msnaive",
                "msoptimized"
        );
    }

    @Override
    public String toCSV(String delimiter) {
        return intercalate(delimiter,
//                repo.getRepositoryName(),
                commit,
                file,
                query.getFunctionName(),
                query.parametersToString(),
                msNaive,
                msOptimized
        );
    }
}
