package org.variantsync.diffdetective.experiments.views.result;

import org.variantsync.diffdetective.util.CSV;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.view.DiffView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

import static org.variantsync.functjonal.Functjonal.intercalate;

/**
 * Single data point in our feasibility study.
 * An object of this class corresponds to a row in the CSV files we export.
 * This row contains all information for benchmarking a single view generation:
 * @param commit The commit on which views were generated.
 * @param file The file of the patch that was analysed.
 * @param relevance The relevance predicate from which the views were generated.
 * @param msNaive Milliseconds it took to generate the view with the {@link DiffView#naive(VariationDiff, Relevance) naive algorithm}
 * @param msOptimized Milliseconds it took to generate the view with the {@link DiffView#optimized(VariationDiff, Relevance) optimized algorithm}
 * @param diffStatistics Various statistics on the variation diff of the analysed patch. 
 * @param viewStatistics The same statistics as for the original variation diff but for the produced view.
 */
public record ViewEvaluation(
//        Repository repo,
        String commit,
        String file,
        Relevance relevance,
        long msNaive,
        long msOptimized,
        DiffStatistics diffStatistics,
        DiffStatistics viewStatistics
) implements CSV {
    /**
     * Holds various information of a variation diff.
     * @param nodeCount The number of nodes contained in the variation diff.
     * @param annotationNodeCount The number of annotation nodes in the variation diff.
     */
    public record DiffStatistics(int nodeCount, int annotationNodeCount) {
        /**
         * Gathers statistics of a given variation diff.
         * This method is side-effect free and will not alter the given diff.
         * @param d A variation diff to extract statistics from.
         * @return The extracted statistics.
         */
        public static DiffStatistics of(final VariationDiff<?> d) {
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

    /**
     * Creates the header for a CSV file in which objects of this class
     * can be rows.
     * @param delimiter The delimiter to use between rows in the CSV file (see {@link CSV#DEFAULT_CSV_DELIMITER}.
     * @return A string that should be the first row in a CSV file with objects of this class
     *         as rows.
     */
    public static String makeHeader(String delimiter) {
        return intercalate(delimiter,
//                "repository",
                "commit",
                "file",
                "vtype",
//                "vargs",
                "msnaive",
                "msoptimized",
                "diffNodeCount",
                "diffAnnotationNodeCount",
                "viewNodeCount",
                "viewAnnotationNodeCount"
        );
    }

    @Override
    public String toCSV(String delimiter) {
        return intercalate(delimiter,
//                repo.getRepositoryName(),
                commit,
                file,
                relevance.getFunctionName(),
//                getQueryArguments(),
                msNaive,
                msOptimized,
                diffStatistics.nodeCount,
                diffStatistics.annotationNodeCount,
                viewStatistics.nodeCount,
                viewStatistics.annotationNodeCount
        );
    }
}
