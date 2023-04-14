package org.variantsync.diffdetective.experiments.views.result;

import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.variation.tree.view.query.Query;
import org.variantsync.diffdetective.variation.tree.view.query.VariantQuery;

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
                "jargs",
                "msnaive",
                "msoptimized"
        );
    }

    private String getQueryArguments() {
        if (query instanceof VariantQuery) {
            return query.parametersToString();
        }
        return "";
    }

    @Override
    public String toCSV(String delimiter) {
        return intercalate(delimiter,
//                repo.getRepositoryName(),
                commit,
                file,
                query.getFunctionName(),
                getQueryArguments(),
                msNaive,
                msOptimized
        );
    }
}
