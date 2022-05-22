package org.variantsync.diffdetective.diff;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.ParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffResult;
import org.variantsync.functjonal.Result;
import org.variantsync.functjonal.iteration.MappedIterator;
import org.variantsync.functjonal.iteration.SideEffectIterator;
import org.variantsync.functjonal.iteration.Yield;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.variantsync.diffdetective.util.StringUtils.LINEBREAK_REGEX;

/**
 * This class creates a GitDiff-object from a git repository (Git-object).
 * <p>
 * The commits from the repository are first filtered using the given DiffFilter.
 * Then a CommitDiff is created from each commit.
 * File changes in each commit are then filtered using the given DiffFilter.
 * Then a PatchDiff is created from each file change.
 * Each PatchDiff contains the DiffTree of its patch.
 *
 * @author Soeren Viegener, Paul Maximilian Bittner
 */
public class GitDiffer {
    private static final Pattern BOM_PATTERN = Pattern.compile("\\x{FEFF}");
    private static final Pattern DIFF_HUNK_PATTERN = Pattern.compile( "^@@\\s-(\\d+).*\\+(\\d+).*@@$");
    private static final Pattern DIFF_HEADER_PATTERN = Pattern.compile( "^\\+\\+\\+.*$", Pattern.MULTILINE);
    private static final String NO_NEW_LINE = "\\ No newline at end of file";

    private final Git git;
    private final DiffFilter diffFilter;
    private final ParseOptions parseOptions;

    public GitDiffer(final Repository repository) {
        this.git = repository.getGitRepo().run();
        this.diffFilter = repository.getDiffFilter();
        this.parseOptions = repository.getParseOptions();
    }

    /**
     * Creates a GitDiff object.
     * For this, each commit is iterated to create CommitDiffs
     *
     * @return The GitDiff object created from the Git object of this GitDiffer
     */
    public GitDiff createGitDiff() {
        final GitDiff gitDiff = new GitDiff();

        final Iterable<RevCommit> commitsIterable;
        try {
            commitsIterable = git.log().call();
        } catch (GitAPIException e) {
            Logger.warn("Could not get log for git repository {}", git.toString());
            return null;
        }

        // we specifically count the commits here because the amount of unfiltered commits is
        // otherwise lost
        final int[] commitAmount = {0};
        final Iterator<RevCommit> commitIterator = new SideEffectIterator<>(
                commitsIterable.iterator(),
                r -> ++commitAmount[0]);
        for (CommitDiffResult commitDiff : loadAllValidIn(commitIterator)) {
            commitDiff.diff().ifPresent(gitDiff::addCommitDiff);
        }
        gitDiff.setCommitAmount(commitAmount[0]);

        return gitDiff;
    }

    public Yield<RevCommit> yieldRevCommits() {
        final Iterable<RevCommit> commitsIterable;
        try {
            commitsIterable = git.log().call();
        } catch (GitAPIException e) {
            Logger.warn("Could not get log for git repository {}", git.toString());
            return null;
        }

        return yieldAllValidIn(commitsIterable.iterator());
    }

    public Yield<RevCommit> yieldRevCommitsAfter(final Function<RevCommit, RevCommit> f) {
        Iterable<RevCommit> commitsIterable;
        try {
            commitsIterable = git.log().call();
        } catch (GitAPIException e) {
            Logger.warn("Could not get log for git repository {}", git.toString());
            return null;
        }

        return yieldAllValidIn(new MappedIterator<>(commitsIterable.iterator(), f));
    }

    public Yield<CommitDiffResult> yieldCommitDiffs() {
        final Iterable<RevCommit> commitsIterable;
        try {
            commitsIterable = git.log().call();
        } catch (GitAPIException e) {
            Logger.warn("Could not get log for git repository {}", git.toString());
            return null;
        }

        return loadAllValidIn(commitsIterable.iterator());
    }

    private Yield<RevCommit> yieldAllValidIn(final Iterator<RevCommit> commitsIterator) {
        return new Yield<>(
                () -> {
                    while (commitsIterator.hasNext()) {
                        final RevCommit c = commitsIterator.next();
                        // If this commit is filtered, go to the next one.
                        if (!diffFilter.filter(c)) {
                            continue;
                        }

                        if (c.getParentCount() == 0) {
//                            Logger.debug("Warning: Cannot create CommitDiff for commit {} because it does not have parents!", c.getId().getName());
                            continue;
                        }

                        return c;
                    }

                    return null;
                }
        );
    }

    private Yield<CommitDiffResult> loadAllValidIn(final Iterator<RevCommit> commitsIterator) {
        return yieldAllValidIn(commitsIterator).map(this::createCommitDiff);
    }

    public CommitDiffResult createCommitDiff(final RevCommit revCommit) {
        return createCommitDiffFromFirstParent(git, diffFilter, revCommit, parseOptions);
    }

    /**
     * Creates a CommitDiff from a given commit.
     * For this, the git diff is retrieved using JGit.
     * For each file in the diff, a PatchDiff is created.
     *
     * @param git The git repo which the commit stems from.
     * @param currentCommit The commit from which to create a CommitDiff
     * @param parseOptions
     * @return The CommitDiff of the given commit
     */
    public static CommitDiffResult createCommitDiffFromFirstParent(
            Git git,
            DiffFilter diffFilter,
            RevCommit currentCommit,
            final ParseOptions parseOptions) {
        if (currentCommit.getParentCount() == 0) {
            return CommitDiffResult.Failure(
                    DiffError.COMMIT_HAS_NO_PARENTS, "Commit " + currentCommit.getId().getName() + " does not have parents");
        }

        final RevCommit parent;
        try {
            parent = new RevWalk(git.getRepository()).parseCommit(currentCommit.getParent(0).getId());
        } catch (IOException e) {
            return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not parse parent commit of " + currentCommit.getId().getName() + "!");
        }
        return createCommitDiff(git, diffFilter, parent, currentCommit, parseOptions);
    }

    /**
     * Creates a CommitDiff from a given commit.
     * For this, the git diff is retrieved using JGit.
     * For each file in the diff, a PatchDiff is created.
     *
     * @param git The git repo which the commit stems from.
     * @return The CommitDiff of the given commit
     */
    public static CommitDiffResult createCommitDiff(
            Git git,
            DiffFilter diffFilter,
            RevCommit parentCommit,
            RevCommit childCommit,
            final ParseOptions parseOptions) {
        // get TreeParsers
        final CanonicalTreeParser currentTreeParser = new CanonicalTreeParser();
        final CanonicalTreeParser prevTreeParser = new CanonicalTreeParser();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            if (childCommit.getTree() == null) {
                return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not obtain RevTree from child commit " + childCommit.getId());
            }
            if (parentCommit.getTree() == null) {
                return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not obtain RevTree from parent commit " + parentCommit.getId());
            }

            try {
                currentTreeParser.reset(reader, childCommit.getTree());
                prevTreeParser.reset(reader, parentCommit.getTree());
            } catch (IOException e) {
                return CommitDiffResult.Failure(DiffError.JGIT_ERROR, e.toString());
            }
        }

        return getPatchDiffs(git, diffFilter, parseOptions, prevTreeParser, currentTreeParser, parentCommit, childCommit);
    }

    /**
     * Creates a CommitDiff from a given commit that compares the given commit with the current working tree.
     * For this, the git diff is retrieved using JGit.
     * For each file in the diff, a PatchDiff is created.
     *
     * @param git The git repo which the commit stems from
     * @param commit The commit which the working tree is compared with
     * @param keepFullDiffs  If true, the PatchDiff will contain the full diff as a string. Set to false if you want to
     *                       reduce memory consumption
     * @return The CommitDiff of the given commit
     */
    public static CommitDiffResult createWorkingTreeDiff(
    		Git git,
    		DiffFilter diffFilter,
    		RevCommit commit,
    		final ParseOptions parseOptions) {
		// get TreeParsers
        final AbstractTreeIterator workingTreeParser = new FileTreeIterator(git.getRepository());
        final CanonicalTreeParser prevTreeParser = new CanonicalTreeParser();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            if (commit.getTree() == null) {
                return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not obtain RevTree from child commit " + commit.getId());
            }

            try {
                prevTreeParser.reset(reader, commit.getTree());
            } catch (IOException e) {
                return CommitDiffResult.Failure(DiffError.JGIT_ERROR, e.toString());
            }
        }
        
        return getPatchDiffs(git, diffFilter, parseOptions, prevTreeParser, workingTreeParser, commit, commit);
    }
    
    /**
     * 
     * @param git The git repo which the commit stems from
     * @param diffFilter {@link DiffFilter}
     * @param parseOptions {@link ParseOptions}
     * @param prevTreeParser The tree parser for parentCommit
     * @param currentTreeParser The tree parser for childCommit or the working tree
     * @param parentCommit The {@link RevCommit} for the parent commit
     * @param childCommit The {@link RevCommit} for the child commit (equal to parentCommit if working tree is requested)
     * @return {@link CommitDiffResult}
     */
    private static CommitDiffResult getPatchDiffs(
    		Git git,
    		DiffFilter diffFilter,
    		final ParseOptions parseOptions,
    		AbstractTreeIterator prevTreeParser,
    		AbstractTreeIterator currentTreeParser,
    		RevCommit parentCommit,
    		RevCommit childCommit) {
    	final CommitDiff commitDiff = new CommitDiff(childCommit, parentCommit);
        final List<DiffError> errors = new ArrayList<>();

        // get PatchDiffs
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DiffFormatter diffFormatter = new DiffFormatter(outputStream))
        {
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setDetectRenames(true);
            diffFormatter.getRenameDetector().setRenameScore(50);

            List<DiffEntry> entries = diffFormatter.scan(prevTreeParser, currentTreeParser);
            for (DiffEntry diffEntry : entries) {
                if (!diffFilter.filter(diffEntry)) {
                    continue;
                }

                diffFormatter.format(diffEntry);
                final String gitDiff = outputStream.toString(StandardCharsets.UTF_8);
                final Result<PatchDiff, DiffError> patchDiff =
                        getBeforeFullFile(git, parentCommit, diffEntry.getOldPath()).unwrap()
                        .bind(file -> createPatchDiff(
                                commitDiff,
                                diffEntry,
                                gitDiff,
                                file,
                                parseOptions).unwrap()
                        );

                patchDiff.ifSuccess(commitDiff::addPatchDiff);
                patchDiff.ifFailure(errors::add);
                outputStream.reset();
            }
        } catch (IOException e) {
            return CommitDiffResult.Failure(DiffError.JGIT_ERROR, e.toString());
        }

        return new CommitDiffResult(Optional.of(commitDiff), errors);
    }
    
    /**
     * Creates a PatchDiff from a given DiffEntry of a commit
     *
     * @param commitDiff     The CommitDiff the created PatchDiff belongs to
     * @param diffEntry      The DiffEntry of the file that was changed in the commit
     * @param gitDiff        The git diff of the file that was changed
     * @param beforeFullFile The full file before the change
     * @return The PatchDiff of the given DiffEntry
     */
    private static DiffResult<PatchDiff> createPatchDiff(
            CommitDiff commitDiff,
            DiffEntry diffEntry,
            final String gitDiff,
            String beforeFullFile,
            final ParseOptions parseOptions) {
        final Matcher matcher = DIFF_HEADER_PATTERN.matcher(gitDiff);
        final String strippedDiff;
        if (matcher.find()) {
            strippedDiff = gitDiff.substring(matcher.end() + 1);
        } else {
            strippedDiff = gitDiff;
        }

        final String fullDiff = getFullDiff(beforeFullFile, strippedDiff);
        final DiffResult<DiffTree> diffTree = DiffTreeParser.createDiffTree(fullDiff, true, true, parseOptions.annotationParser());

//        if (diffTree.isFailure()) {
//            Logger.debug("Something went wrong parsing patch for file {} at commit {}!",
//                    diffEntry.getOldPath(), commitDiff.getAbbreviatedCommitHash());
//        }

        return diffTree.map(t -> {
            // not storing the full diff reduces memory usage by around 40-50%
            final String diffToRemember = switch (parseOptions.diffStoragePolicy()) {
                case DO_NOT_REMEMBER -> "";
                case REMEMBER_DIFF -> gitDiff;
                case REMEMBER_FULL_DIFF -> fullDiff;
                case REMEMBER_STRIPPED_DIFF -> strippedDiff;
            };

            return new PatchDiff(commitDiff, diffEntry, diffToRemember, t);
        });
    }

    /**
     * Creates a full git diff from a file before the change and the git diff containing only the
     * changed lines
     *
     * @param beforeFile The full file before the change
     * @param gitDiff    The git diff containing only the changed lines
     * @return A full git diff containing the complete file and all changes
     */
    public static String getFullDiff(String beforeFile, String gitDiff) {
        String[] beforeLines = LINEBREAK_REGEX.split(beforeFile, -1);
        String[] diffLines = LINEBREAK_REGEX.split(gitDiff);

        int beforeIndex = 0;

        List<String> fullDiffLines = new ArrayList<>();

        for (String diffLine : diffLines) {
            Matcher matcher = DIFF_HUNK_PATTERN.matcher(diffLine);

            if (matcher.find()) {
                // found diffHunkRegex

                // subtract 1 because line numbers start at 1
                int beforeDiffIndex = Integer.parseInt(matcher.group(1)) - 1;

                while (beforeIndex < beforeDiffIndex) {
                    fullDiffLines.add(" " + beforeLines[beforeIndex]);
                    beforeIndex++;
                }
            } else if (diffLine.equals(NO_NEW_LINE)) {
                fullDiffLines.add("\n");
            } else {
                fullDiffLines.add(diffLine);
                if (!diffLine.startsWith("+")) {
                    beforeIndex++;
                }
            }
        }
        while (beforeIndex < beforeLines.length) {
            fullDiffLines.add(" " + beforeLines[beforeIndex]);
            beforeIndex++;
        }
        String fullDiff = String.join("\n", fullDiffLines);

        // JGit seems to put BOMs in weird locations somewhere in the files
        // We need to remove those or the regex matching for the lines fails
        fullDiff = BOM_PATTERN.matcher(fullDiff).replaceAll("");

        return fullDiff;
    }

    /**
     * Gets the full content of a file before a commit
     *
     * @param commit   The commit in which the file was changed
     * @param filename The name of the file
     * @return The full content of the file before the commit
     */
    public static DiffResult<String> getBeforeFullFile(Git git, RevCommit commit, String filename) {
        RevTree tree = commit.getTree();

        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filename));

            // Look for the first file that matches filename.
            if (!treeWalk.next()) {
                return DiffResult.Failure(DiffError.COULD_NOT_OBTAIN_FULLDIFF, "Could not obtain full diff of file " + filename + " before commit " + commit + "!");
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = git.getRepository().open(objectId);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            loader.copyTo(stream);
            return DiffResult.Success(stream.toString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return DiffResult.Failure(DiffError.COULD_NOT_OBTAIN_FULLDIFF, "Could not obtain full diff of file " + filename + " before commit " + commit + "!");
        }
    }
    
    public Git getJGitRepo() {
    	return git;
    }
    
}
