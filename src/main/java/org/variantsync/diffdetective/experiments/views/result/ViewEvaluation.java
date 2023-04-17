package org.variantsync.diffdetective.experiments.views.result;

import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.tree.view.query.Query;
import org.variantsync.diffdetective.variation.tree.view.query.VariantQuery;

import static org.variantsync.functjonal.Functjonal.intercalate;

public record ViewEvaluation(
//        Repository repo,
        String commit,
        String file,
        Query query,
        long msNaive,
        long msOptimized,
        DiffStatistics diffStatistics,
        DiffStatistics viewStatistics
) implements CSV {
    public record DiffStatistics(int nodeCount, int annotationNodeCount) {
        public static DiffStatistics of(final DiffTree d) {
            final int[] nodeCount = {0};
            final int[] annotationNodeCount = {0};

            d.forAll(n -> {
                ++nodeCount[0];
                if (n.isAnnotation()) {
                    ++annotationNodeCount[0];
                }
            });

            return new DiffStatistics(nodeCount[0], annotationNodeCount[0]);
        }
    }

    public static String makeHeader(String delimiter) {
        return intercalate(delimiter,
//                "repository",
                "commit",
                "file",
                "jtype",
                "jargs",
                "msnaive",
                "msoptimized",
                "diffNodeCount",
                "diffAnnotationNodeCount",
                "viewNodeCount",
                "viewAnnotationNodeCount"
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
                msOptimized,
                diffStatistics.nodeCount,
                diffStatistics.annotationNodeCount,
                viewStatistics.nodeCount,
                viewStatistics.annotationNodeCount
        );
    }
}
