package org.variantsync.diffdetective.relationshipedges;

import org.eclipse.jgit.revwalk.RevCommit;
import org.prop4j.Node;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.analysis.CommitHistoryAnalysisTask;
import org.variantsync.diffdetective.analysis.CommitProcessTime;
import org.variantsync.diffdetective.analysis.HistoryAnalysis;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.metadata.ExplainedFilterSummary;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.util.Clock;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.functjonal.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Task for performing the mining for Lukas Güthing's thesis.
 *
 * @author Paul Bittner, Lukas Güthing
 */
public class PatternValidationTask extends CommitHistoryAnalysisTask {
    public PatternValidationTask(Options options) {
        super(options);
    }

    @Override
    public AnalysisResult call() throws Exception {
        // Setup. Obtain the result from the initial setup in the super class.
        final AnalysisResult miningResult = super.call();
        final DiffTreeLineGraphExportOptions exportOptions = options.exportOptions();
        // List to store the process time of each commit.
        final List<CommitProcessTime> commitTimes = new ArrayList<>(HistoryAnalysis.COMMITS_TO_PROCESS_PER_THREAD_DEFAULT);
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
                options.analysisStrategy().onCommit(commitDiff, "");


                int numDiffTrees = 0;
                int numRelEdges = 0;
                for (final PatchDiff patch : commitDiff.getPatchDiffs()) {
                    if (patch.isValid()) {
                        final DiffTree t = patch.getDiffTree();
                        DiffTreeTransformer.apply(exportOptions.treePreProcessing(), t);
                        t.assertConsistency();

                        if (!exportOptions.treeFilter().test(t)) {
                            continue;
                        }

                        ++numDiffTrees;

                        boolean RELATIONSHIP_EDGES = true; // TODO: replace this later with some sort of options parameter

                        if (RELATIONSHIP_EDGES) {
                        /*
                        extend the @DiffTree t with relationship edges
                        */
                            EdgeTypedTreeDiff edgeTypedTreeDiff = new EdgeTypedTreeDiff(t);
                            ArrayList<RelationshipEdge> implicationEdges = new ArrayList<>();
                            ArrayList<RelationshipEdge> alternativeEdges = new ArrayList<>();
                            List<DiffNode> annotationNodes = t.computeAnnotationNodes();
                            List<DiffNode> ifNodes = t.computeAllNodesThat(DiffNode::isIf);
                            for (int i = 0; i < ifNodes.size(); i++) {
                                for (int j = i + 1; j < ifNodes.size(); j++) {
                                    if (Implication.areInRelation(ifNodes.get(i), ifNodes.get(j))) {
                                        implicationEdges.add(new RelationshipEdge<Implication>(Implication.class, ifNodes.get(i), ifNodes.get(j)));
                                    }
                                    if (Implication.areInRelation(ifNodes.get(j), ifNodes.get(i))) {
                                        implicationEdges.add(new RelationshipEdge<Implication>(Implication.class, ifNodes.get(j), ifNodes.get(i)));
                                    }
                                    if (Alternative.areInRelation(ifNodes.get(j), ifNodes.get(i))) {
                                        alternativeEdges.add(new RelationshipEdge<Alternative>(Alternative.class, ifNodes.get(i), ifNodes.get(j)));
                                        alternativeEdges.add(new RelationshipEdge<Alternative>(Alternative.class, ifNodes.get(j), ifNodes.get(i)));
                                    }
                                }
                            }
                            numRelEdges += implicationEdges.size();
                            numRelEdges += alternativeEdges.size();
                            edgeTypedTreeDiff.addEdgesWithType(Implication.class, implicationEdges);
                            edgeTypedTreeDiff.addEdgesWithType(Alternative.class, alternativeEdges);
                        }
                    }
                }
                miningResult.relationshipEdges += numRelEdges;
                miningResult.exportedTrees += numDiffTrees;
                miningResult.filterHits.append(new ExplainedFilterSummary(exportOptions.treeFilter()));
                exportOptions.treeFilter().resetExplanations();

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
        return miningResult;
    }
}
