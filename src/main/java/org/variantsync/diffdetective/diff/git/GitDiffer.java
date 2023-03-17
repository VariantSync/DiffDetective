package org.variantsync.diffdetective.diff.git;

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
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.preliminary.GitDiff;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.functjonal.iteration.MappedIterator;
import org.variantsync.functjonal.iteration.SideEffectIterator;
import org.variantsync.functjonal.iteration.Yield;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class creates a GitDiff-object from a git repository (Git-object).
 * <p>
 * The commits from the repository are first filtered using the given DiffFilter.
 * Then a CommitDiff is created for each commit.
 * File changes in each commit are filtered using the given DiffFilter.
 * Then a PatchDiff is created from each file change.
 * Finally, each patch is parsed to a DiffTree.
 *
 * @author Soeren Viegener, Paul Maximilian Bittner
 */
public class GitDiffer {
    private static final Pattern BOM_PATTERN = Pattern.compile("\\x{FEFF}");
    private static final Pattern DIFF_HUNK_PATTERN = Pattern.compile( "^@@\\s-(\\d+).*\\+(\\d+).*@@$");
    private static final Pattern GIT_HEADER_PATTERN = Pattern.compile( "^diff --git .*$", Pattern.MULTILINE);
    private static final Pattern DIFF_HEADER_PATTERN = Pattern.compile( "^\\+\\+\\+.*$", Pattern.MULTILINE);
    private static final String NO_NEW_LINE = "\\ No newline at end of file";

    private final Git git;
    private final DiffFilter diffFilter;
    private final PatchDiffParseOptions parseOptions;

    /**
     * Create a differ operating on the given repository.
     * @param repository The repository for whose history to obtain diffs.
     */
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
    @Deprecated
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
        for (CommitDiffResult commitDiff : yieldAllValidIn(commitIterator).map(this::createCommitDiff)) {
            commitDiff.diff().ifPresent(gitDiff::addCommitDiff);
        }
        gitDiff.setCommitAmount(commitAmount[0]);

        return gitDiff;
    }

    /**
     * Returns all commits in the repository's history.
     */
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

    /**
     * The same as {@link GitDiffer#yieldRevCommits()} but applies the given function f to each commit
     * before returning it.
     * @param f A function to map over all commits before they can be accessed.
     *          Each returned commit was processed by f exactly once.
     * @return All commits in the repository's history after applying the given function to each commit.
     */
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

    /**
     * Filters all undesired commits from the given set of commits using the {@link DiffFilter} of
     * this differs repository.
     * @see GitDiffer#GitDiffer(Repository)
     * @param commitsIterator Commits to filter.
     * @return All commits from the given set that should not be filtered.
     */
    private Yield<RevCommit> yieldAllValidIn(final Iterator<RevCommit> commitsIterator) {
        return new Yield<>(
                () -> {
                    while (commitsIterator.hasNext()) {
                        final RevCommit c = commitsIterator.next();
                        // If this commit is filtered, go to the next one.
                        // filter returns true if we want to include the commit
                        // so if we do not want to filter it, we do not want to have it. Thus skip.
                        if (!diffFilter.filter(c)) {
                            continue;
                        }

                        return c;
                    }

                    return null;
                }
        );
    }

    public CommitDiffResult createCommitDiff(final String commitHash) throws IOException {
        Assert.assertNotNull(git);
        try (var revWalk = new RevWalk(git.getRepository())) {
            final RevCommit commit = revWalk.parseCommit(ObjectId.fromString(commitHash));
            return createCommitDiff(commit);
        }
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
            final PatchDiffParseOptions parseOptions) {
        final RevCommit parent;
        if (currentCommit.getParentCount() > 0) {
            try (var revWalk = new RevWalk(git.getRepository())) {
                parent = revWalk.parseCommit(currentCommit.getParent(0).getId());
            } catch (IOException e) {
                return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not parse parent commit of " + currentCommit.getId().getName() + "!");
            }
        } else {
            parent = null;
        }

        return createCommitDiff(git, diffFilter, parent, currentCommit, parseOptions);
    }

    /**
     * Creates a CommitDiff that describes all changes made by the
     * given childCommit to the given parentCommit.
     *
     * @param git The git repo which the commits stem from.
     * @return The CommitDiff describing all changes between the two commits.
     */
    public static CommitDiffResult createCommitDiff(
            Git git,
            DiffFilter diffFilter,
            RevCommit parentCommit,
            RevCommit childCommit,
            final PatchDiffParseOptions parseOptions) {
        if (childCommit.getTree() == null) {
            return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not obtain RevTree from child commit " + childCommit.getId());
        }
        if (parentCommit != null && parentCommit.getTree() == null) {
            return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not obtain RevTree from parent commit " + parentCommit.getId());
        }

        // get TreeParsers
        final CanonicalTreeParser currentTreeParser = new CanonicalTreeParser();
        final CanonicalTreeParser prevTreeParser = new CanonicalTreeParser();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            try {
                currentTreeParser.reset(reader, childCommit.getTree());
                if (parentCommit != null) {
                    prevTreeParser.reset(reader, parentCommit.getTree());
                }
            } catch (IOException e) {
                return CommitDiffResult.Failure(DiffError.JGIT_ERROR, e.toString());
            }
        }

        final AbstractTreeIterator parentTreeIterator;
        if (parentCommit == null) {
            parentTreeIterator = new EmptyTreeIterator();
        } else {
            parentTreeIterator = prevTreeParser;
        }

        return getPatchDiffs(
                git,
                diffFilter,
                parseOptions,
                parentTreeIterator,
                currentTreeParser,
                parentCommit,
                childCommit
        );
    }

    /**
     * The same as {@link GitDiffer#createCommitDiff(Git, DiffFilter, RevCommit, RevCommit, PatchDiffParseOptions)}
     * but diffs the given commit against the current working tree.
     *
     * @param git The git repo which the commit stems from
     * @param commit The commit which the working tree is compared with
     * @param parseOptions {@link PatchDiffParseOptions}
     * @return The CommitDiff of the given commit
     */
    public static CommitDiffResult createWorkingTreeDiff(
    		Git git,
    		DiffFilter diffFilter,
    		RevCommit commit,
    		final PatchDiffParseOptions parseOptions) {
        if (commit != null && commit.getTree() == null) {
            return CommitDiffResult.Failure(DiffError.JGIT_ERROR, "Could not obtain RevTree from child commit " + commit.getId());
        }

		// get TreeParsers
        final AbstractTreeIterator workingTreeIterator = new FileTreeIterator(git.getRepository());
        final AbstractTreeIterator prevTreeIterator;
        if (commit == null) {
            prevTreeIterator = new EmptyTreeIterator();
        } else try (ObjectReader reader = git.getRepository().newObjectReader()) {
            prevTreeIterator = new CanonicalTreeParser(null, reader, commit.getTree());
        } catch (IOException e) {
            return CommitDiffResult.Failure(DiffError.JGIT_ERROR, e.toString());
        }

        return getPatchDiffs(git, diffFilter, parseOptions, prevTreeIterator, workingTreeIterator, commit, commit);
    }
    
    /**
     * Obtains the CommitDiff between two commit's trees.
     *
     * @param git The git repo which the commit stems from
     * @param diffFilter {@link DiffFilter}
     * @param parseOptions {@link PatchDiffParseOptions}
     * @param prevTreeParser The tree parser for parentCommit
     * @param currentTreeParser The tree parser for childCommit or the working tree
     * @param parentCommit The {@link RevCommit} for the parent commit
     * @param childCommit The {@link RevCommit} for the child commit (equal to parentCommit if working tree is requested)
     * @return {@link CommitDiffResult}
     */
    private static CommitDiffResult getPatchDiffs(
    		Git git,
    		DiffFilter diffFilter,
    		final PatchDiffParseOptions parseOptions,
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
                final String filename = diffEntry.getOldPath();

                final Matcher matcher = DIFF_HEADER_PATTERN.matcher(gitDiff);
                final String strippedDiff;
                if (matcher.find()) {
                    strippedDiff = gitDiff.substring(matcher.end() + 1);
                } else if (GIT_HEADER_PATTERN.matcher(gitDiff).find()) {
                    // Check whether it is a diff returned by `git diff` and not one created by some other means
                    strippedDiff = "";
                } else {
                    // It is a diff from another source (e.g., manually created or copy-pasted from GitHub)
                    strippedDiff = gitDiff;
                }


                try {
                    final String fullDiff = switch (diffEntry.getChangeType()) {
                        case ADD, DELETE -> {
                            if (strippedDiff.isEmpty()) {
                                // Addition or deletion of an empty file
                                yield "";
                            }
                            // The first lines contains meta information "@@ ... " that we want to skip.
                            final String[] hunkBeginAndRest = StringUtils.LINEBREAK_REGEX.split(strippedDiff, 2);
                            Assert.assertTrue(hunkBeginAndRest.length == 2, "Hunk is only one line. Is it a hunk? Hunk: " + strippedDiff);
                            yield hunkBeginAndRest[1];
                        }
                        case RENAME, COPY, MODIFY -> {
                            final BufferedReader beforeFullFile = getBeforeFullFile(git, parentCommit, filename);
                            yield getFullDiff(beforeFullFile, new BufferedReader(new StringReader(strippedDiff)));
                        }
                    };

                    final DiffTree diffTree = DiffTreeParser.createDiffTree(
                            fullDiff,
                            parseOptions.diffTreeParseOptions()
                    );

                    // not storing the full diff reduces memory usage by around 40-50%
                    final String diffToRemember = switch (parseOptions.diffStoragePolicy()) {
                        case DO_NOT_REMEMBER -> "";
                        case REMEMBER_DIFF -> gitDiff;
                        case REMEMBER_FULL_DIFF -> fullDiff;
                        case REMEMBER_STRIPPED_DIFF -> strippedDiff;
                    };

                    commitDiff.addPatchDiff(new PatchDiff(
                            commitDiff,
                            diffEntry,
                            diffToRemember,
                            diffTree
                    ));
                } catch (IOException e) {
                    Logger.debug(e, "Could not obtain full diff of file " + filename + " before commit " + parentCommit + "!");
                    errors.add(DiffError.COULD_NOT_OBTAIN_FULLDIFF);
                } catch (DiffParseException e) {
                    errors.add(e.getError());
                }

                outputStream.reset();
            }
        } catch (IOException e) {
            return CommitDiffResult.Failure(DiffError.JGIT_ERROR, e.toString());
        }

        return new CommitDiffResult(Optional.of(commitDiff), errors);
    }

    /**
     * Creates a full git diff from a file before the change and the git diff containing only the
     * changed lines.
     *
     * @param beforeFile The full file before the change
     * @param gitDiff    The git diff containing only the changed lines
     * @return A full git diff containing the complete file and all changes
     */
    public static String getFullDiff(BufferedReader beforeFile, BufferedReader gitDiff) {
        try {
            LineNumberReader before = new LineNumberReader(beforeFile);

            List<String> fullDiffLines = new ArrayList<>();

            String diffLine;
            while ((diffLine = gitDiff.readLine()) != null) {
                Matcher matcher = DIFF_HUNK_PATTERN.matcher(diffLine);

                if (matcher.find()) {
                    // found diffHunkRegex

                    // subtract 1 because line numbers start at 1
                    int beforeDiffIndex = Integer.parseInt(matcher.group(1)) - 1;

                    while (before.getLineNumber() < beforeDiffIndex) {
                        fullDiffLines.add(" " + before.readLine());
                    }
                } else if (diffLine.equals(NO_NEW_LINE)) {
                    fullDiffLines.add(StringUtils.LINEBREAK);
                } else {
                    fullDiffLines.add(diffLine);
                    if (!diffLine.startsWith("+")) {
                        before.readLine();
                    }
                }
            }

            String beforeLine;
            while ((beforeLine = before.readLine()) != null) {
                fullDiffLines.add(" " + beforeLine);
            }
            String fullDiff = String.join(StringUtils.LINEBREAK, fullDiffLines);

            // JGit seems to put BOMs in weird locations somewhere in the files
            // We need to remove those or the regex matching for the lines fails
            fullDiff = BOM_PATTERN.matcher(fullDiff).replaceAll("");

            return fullDiff;
        } catch (IOException e) {
            // Going up the call chain, at can be seen, that all callers need functions which do
            // not throw any checked exception, so just rethrow an unchecked one.
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets the full content of a file before a commit.
     *
     * @param commit   The commit in which the file was changed
     * @param filename The name of the file
     * @return The full content of the file before the commit
     */
    public static BufferedReader getBeforeFullFile(Git git, RevCommit commit, String filename) throws IOException {
        RevTree tree = commit.getTree();

        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filename));

            // Look for the first file that matches filename.
            if (!treeWalk.next()) {
                throw new FileNotFoundException("Couldn't find " + filename + " in the commit " + commit);
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = git.getRepository().open(objectId);
            return new BufferedReader(new InputStreamReader(loader.openStream()));
        }
    }

    /**
     * Returns the internal representation of this differs repository.
     */
    public Git getJGitRepo() {
        return git;
    }
}
