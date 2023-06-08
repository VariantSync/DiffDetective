package org.variantsync.diffdetective.relationshipedges;

import org.eclipse.jgit.revwalk.RevCommit;
import org.prop4j.False;
import org.prop4j.True;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.*;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExport;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.variantsync.diffdetective.relationshipedges.Validation.ValidationExportOptions;

/**
 * Task for performing the mining for the paper evaluation.
 *
 *
 */
public class PaperEvaluationTask extends CommitHistoryAnalysisTask {
    public PaperEvaluationTask(Options options) {
        super(options);
    }

    @Override
    public AnalysisResult call() throws Exception {
        // Setup. Obtain the result from the initial setup in the super class.
        final AnalysisResult miningResult = super.call();
        // List to store the process time of each commit.
        final List<CommitProcessTime> commitTimes = new ArrayList<>(HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);

        final List<PatchProcessDetails> patchDetails = new ArrayList<>();
        // Clock for runtime measurement.
        final Clock totalTime = new Clock();
        totalTime.start();
        final Clock commitProcessTimer = new Clock();
        // For each commit:
        for (final RevCommit commit : options.commits()) {
            try {
                commitProcessTimer.start();

                // parse the commit
                final CommitDiffResult commitDiffResult = options.differ().createCommitDiff(commit);

                // report any errors that occurred and exit in case no DiffTree could be parsed.
                miningResult.reportDiffErrors(commitDiffResult.errors());
                if (commitDiffResult.diff().isEmpty()) {
                    Logger.debug("[MiningTask::call] found commit that failed entirely and was not filtered because:\n{}", commitDiffResult.errors());
                    ++miningResult.failedCommits;
                    continue;
                }

                // extract the produced commit diff and inform the strategy
                final CommitDiff commitDiff = commitDiffResult.diff().get();
                // options.analysisStrategy().onCommit(commitDiff, "");

                int numDiffTrees = 0;
                int numImplEdges = 0;
                int numAltEdges = 0;
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(options.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!options.treeFilter().test(t)) {
                            continue;
                        }

                        ++numDiffTrees;

                        boolean RELATIONSHIP_EDGES = true;  // Used for comparison between runtime with and without edge-adding
                        boolean optimized = true;           // Used for comparison between optimized (using mutual exclusivity) and not optimized runtime
                        boolean alternativeFirst = false;   // When optimized, used for comparing runtimes checking for alternative instead of implication first

                        if (RELATIONSHIP_EDGES) {
                        /*
                        extend the @DiffTree t with relationship edges
                        */
                            Clock additionalTime = new Clock();
                            EdgeTypedDiff edgeTypedDiff = new EdgeTypedDiff(t);
                            ArrayList<RelationshipEdge<? extends RelationshipType>> implicationEdges = new ArrayList<>();
                            ArrayList<RelationshipEdge<? extends RelationshipType>> alternativeEdges = new ArrayList<>();
                            List<DiffNode> annotationNodes = t.computeAnnotationNodes();
                            List<DiffNode> ifNodes = t.computeAllNodesThat(DiffNode::isIf);
                            for (int i = 0; i < ifNodes.size(); i++) {
                                if (ifNodes.get(i).getDirectFeatureMapping().equals(new False())|| ifNodes.get(i).getDirectFeatureMapping().equals(new True())) continue; // exclude nodes with Formula "True"/"False" (equivalent to "#if 1"/"if 0")
                                for (int j = i + 1; j < ifNodes.size(); j++) {
                                    if (ifNodes.get(j).getDirectFeatureMapping().equals(new False())|| ifNodes.get(j).getDirectFeatureMapping().equals(new True())) continue; // exclude nodes with Formula "True"/"False" (equivalent to "#if 1"/"if 0")
                                    if(optimized){
                                        if(alternativeFirst){
                                            if (Alternative.areInRelation(ifNodes.get(i), ifNodes.get(j))) { // we only need to check one way since alternative edges are always bi-directional
                                                alternativeEdges.add(new RelationshipEdge<>(Alternative.class, ifNodes.get(i), ifNodes.get(j)));
                                                alternativeEdges.add(new RelationshipEdge<>(Alternative.class, ifNodes.get(j), ifNodes.get(i)));
                                            } else { // we only need to check for implication relation if both nodes are not alternative to each other
                                                if (Implication.areInRelation(ifNodes.get(i), ifNodes.get(j))) {
                                                    implicationEdges.add(new RelationshipEdge<>(Implication.class, ifNodes.get(i), ifNodes.get(j)));
                                                }
                                                if (Implication.areInRelation(ifNodes.get(j), ifNodes.get(i))) {
                                                    implicationEdges.add(new RelationshipEdge<>(Implication.class, ifNodes.get(j), ifNodes.get(i)));
                                                }
                                            }
                                        }else{
                                            boolean checkAlternative = true;
                                            if (Implication.areInRelation(ifNodes.get(i), ifNodes.get(j))) {
                                                implicationEdges.add(new RelationshipEdge<>(Implication.class, ifNodes.get(i), ifNodes.get(j)));
                                                checkAlternative = false;
                                            }
                                            if (Implication.areInRelation(ifNodes.get(j), ifNodes.get(i))) {
                                                implicationEdges.add(new RelationshipEdge<>(Implication.class, ifNodes.get(j), ifNodes.get(i)));
                                                checkAlternative = false;
                                            }
                                            if(checkAlternative){ // we only need to check for alternative relation if nodes are not already in implication relation
                                                if (Alternative.areInRelation(ifNodes.get(i), ifNodes.get(j))) { // we only need to check one way since alternative edges are always bi-directional
                                                    alternativeEdges.add(new RelationshipEdge<>(Alternative.class, ifNodes.get(i), ifNodes.get(j)));
                                                    alternativeEdges.add(new RelationshipEdge<>(Alternative.class, ifNodes.get(j), ifNodes.get(i)));
                                                }
                                            }
                                        }
                                    }else{
                                        if (Alternative.areInRelation(ifNodes.get(i), ifNodes.get(j))) { // we only need to check one way since alternative edges are always bi-directional
                                            alternativeEdges.add(new RelationshipEdge<>(Alternative.class, ifNodes.get(i), ifNodes.get(j)));
                                            alternativeEdges.add(new RelationshipEdge<>(Alternative.class, ifNodes.get(j), ifNodes.get(i)));
                                        }
                                        if (Implication.areInRelation(ifNodes.get(i), ifNodes.get(j))) {
                                            implicationEdges.add(new RelationshipEdge<>(Implication.class, ifNodes.get(i), ifNodes.get(j)));
                                        }
                                        if (Implication.areInRelation(ifNodes.get(j), ifNodes.get(i))) {
                                            implicationEdges.add(new RelationshipEdge<>(Implication.class, ifNodes.get(j), ifNodes.get(i)));
                                        }
                                    }

                                }
                            }
                            numImplEdges += implicationEdges.size();
                            numAltEdges += alternativeEdges.size();
                            edgeTypedDiff.addEdgesWithType(Implication.class, implicationEdges);
                            edgeTypedDiff.addEdgesWithType(Alternative.class, alternativeEdges);

                            /* measure the complexity change from variation diffs to edge-typed variation diffs
                             * with a simple metric: (#relation edges/#nesting edges)
                             */
                            int addedComplexityPercents = (int) (edgeTypedDiff.calculateAdditionalComplexity() * 100);
                            if(edgeTypedDiff.calculateAdditionalComplexity() == 0){
                                miningResult.complexityChangeCount[0] += 1;
                            } else if (addedComplexityPercents == 0 ||  isBetween(addedComplexityPercents, 0, 5)) {
                                miningResult.complexityChangeCount[1] += 1;
                            } else if (isBetween(addedComplexityPercents, 5, 10)) {
                                miningResult.complexityChangeCount[2] += 1;
                            } else if (isBetween(addedComplexityPercents, 10, 20)) {
                                miningResult.complexityChangeCount[3] += 1;
                            } else if (isBetween(addedComplexityPercents, 20, 40)) {
                                miningResult.complexityChangeCount[4] += 1;
                            } else if (isBetween(addedComplexityPercents, 40, 60)) {
                                miningResult.complexityChangeCount[5] += 1;
                            } else {
                                miningResult.complexityChangeCount[6] += 1;
                            }

                            if(edgeTypedDiff.calculateAdditionalComplexity() != 0) patch.setEdgeTypedDiff(edgeTypedDiff);
                            var time = additionalTime.getPassedMilliseconds();
                            miningResult.edgeAddingRuntimeInMilliseconds += time;
                            patchDetails.add(new PatchProcessDetails(commitDiff.getCommitHash(), patch.getFileName(), options.repository().getRepositoryName(), time, addedComplexityPercents, implicationEdges.size(), alternativeEdges.size(), t.computeSize(), ifNodes.size()));
                        }
                    }

                }
                miningResult.implicationEdges += numImplEdges;
                miningResult.alternativeEdges += numAltEdges;
                miningResult.exportedTrees += numDiffTrees;
                miningResult.filterHits.append(new ExplainedFilterSummary(options.treeFilter()));
                options.treeFilter().resetExplanations();

                // Report the commit process time if the commit is not empty.
                if (numDiffTrees > 0) {
                    final long commitTimeMS = commitProcessTimer.getPassedMilliseconds();
                    // find max commit time
                    if (commitTimeMS > miningResult.max.milliseconds()) {
                        miningResult.max.set(commitDiff.getCommitHash(), commitTimeMS);
                    }
                    // find min commit time
                    if (commitTimeMS < miningResult.min.milliseconds()) {
                        miningResult.min.set(commitDiff.getCommitHash(), commitTimeMS);
                    }
                    // report time
                    commitTimes.add(new CommitProcessTime(commitDiff.getCommitHash(), options.repository().getRepositoryName(), commitTimeMS));
                    ++miningResult.exportedCommits;
                } else {
                    ++miningResult.emptyCommits;
                }

                var lineGraphDestination = options.analysisStrategy().onCommit(commitDiff);
                var exportAnalysisResult = LineGraphExport.toLineGraphFormatEdgeTyped(commitDiff, ValidationExportOptions(options.repository()), lineGraphDestination);
                exportAnalysisResult.exportedCommits = 0; // workaround for wrong exportedCommits numbers after semigroup appending
                miningResult.append(exportAnalysisResult);


            } catch (Exception e) {
                Logger.error(e, "An unexpected error occurred at {} in {}", commit.getId().getName(), getOptions().repository().getRepositoryName());
                throw e;
            }
        }
        // shutdown; report total time; export results
        options.analysisStrategy().end();
        miningResult.runtimeInSeconds = totalTime.getPassedSeconds();
        miningResult.exportTo(FileUtils.addExtension(options.outputDir(), AnalysisResult.EXTENSION));
        exportCommitTimes(commitTimes, FileUtils.addExtension(options.outputDir(), COMMIT_TIME_FILE_EXTENSION));
        exportPatchDetails(patchDetails, FileUtils.addExtension(options.outputDir(), ".patchdetail.txt"), false);
        exportPatchDetails(patchDetails, FileUtils.addExtension(options.outputDir(), ".patchdetail.csv"), true);
        return miningResult;
    }

    public static void exportPatchDetails(final List<PatchProcessDetails> patchDetails, final Path pathToOutputFile, boolean asCsv) {
        if(patchDetails.isEmpty()) return;

        final StringBuilder times = new StringBuilder();

        for (final PatchProcessDetails pD : patchDetails) {
            if(asCsv){
                times.append(pD.toCSV()).append(StringUtils.LINEBREAK);
            }else {
                times.append(pD.toString()).append(StringUtils.LINEBREAK);
            }
        }

        try {
            IO.write(pathToOutputFile, times.toString());
        } catch (IOException e) {
            Logger.error(e);
            System.exit(1);
        }
    }
    boolean isBetween(int x, int a, int b){
        return (x <= b && x > a);
    }
}
