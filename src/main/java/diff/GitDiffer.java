package diff;

import datasets.ParseOptions;
import datasets.Repository;
import de.variantsync.functjonal.Product;
import de.variantsync.functjonal.Result;
import de.variantsync.functjonal.iteration.SideEffectIterator;
import de.variantsync.functjonal.iteration.Yield;
import diff.difftree.DiffTree;
import diff.difftree.parse.DiffTreeParser;
import diff.result.CommitDiffResult;
import diff.result.DiffError;
import diff.result.DiffResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.pmw.tinylog.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static diff.result.DiffError.*;

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
    public static final String BOM_REGEX = "\\x{FEFF}";
    public static final String DIFF_HUNK_REGEX = "^@@\\s-(\\d+).*\\+(\\d+).*@@$";
    public static final String DIFF_HEADER_REGEX = "^\\+\\+\\+.*$";
    public static final String NO_NEW_LINE = "\\ No newline at end of file";

    private final Git git;
    private final DiffFilter diffFilter;
    private final ParseOptions parseOptions;

    public GitDiffer(final Repository repository) {
        this.git = repository.load();
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
            commitDiff.unwrap().first().ifPresent(gitDiff::addCommitDiff);
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
                        // TODO: Log filter hits
                        final RevCommit c = commitsIterator.next();
                        // If this commit is filtered, go to the next one.
                        if (!diffFilter.filter(c)) {
                            continue;
                        }

                        if (c.getParentCount() == 0) {
//                            Logger.debug("Warning: Cannot create CommitDiff for commit " + c.getId().getName() + " because it does not have parents!");
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
                    COMMIT_HAS_NO_PARENTS, "Commit " + currentCommit.getId().getName() + " does not have parents");
        }

        return createCommitDiff(git, diffFilter, currentCommit.getParent(0), currentCommit, parseOptions);
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
                return CommitDiffResult.Failure(JGIT_ERROR, "Could not obtain RevTree from child commit " + childCommit.getId());
            }
            if (parentCommit.getTree() == null) {
                return CommitDiffResult.Failure(JGIT_ERROR, "Could not obtain RevTree from parent commit " + parentCommit.getId());
            }

            try {
                currentTreeParser.reset(reader, childCommit.getTree());
                prevTreeParser.reset(reader, parentCommit.getTree());
            } catch (IOException e) {
                return CommitDiffResult.Failure(JGIT_ERROR, e.toString());
            }
        }

        final CommitDiff commitDiff = new CommitDiff(childCommit, parentCommit);
        final Product<Optional<CommitDiff>, List<DiffError>> result = new Product<>(
                Optional.of(commitDiff),
                new ArrayList<>()
        );

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
                patchDiff.ifFailure(error -> result.second().add(error));
                outputStream.reset();
            }
        } catch (IOException e) {
            return CommitDiffResult.Failure(JGIT_ERROR, e.toString());
        }

        return new CommitDiffResult(result);
    }

    /**
     * Creates a CommitDiff from a given commit.
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
                return CommitDiffResult.Failure(JGIT_ERROR, "Could not obtain RevTree from child commit " + commit.getId());
            }

            try {
                prevTreeParser.reset(reader, commit.getTree());
            } catch (IOException e) {
                return CommitDiffResult.Failure(JGIT_ERROR, e.toString());
            }
        }

        final CommitDiff commitDiff = new CommitDiff(commit, commit);
        final Product<Optional<CommitDiff>, List<DiffError>> result = new Product<>(
                Optional.of(commitDiff),
                new ArrayList<>()
        );

        // get PatchDiffs
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DiffFormatter diffFormatter = new DiffFormatter(outputStream))
        {
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setDetectRenames(true);
            diffFormatter.getRenameDetector().setRenameScore(50);

            List<DiffEntry> entries = diffFormatter.scan(prevTreeParser, workingTreeParser);
            for (DiffEntry diffEntry : entries) {
                if (!diffFilter.filter(diffEntry)) {
                    continue;
                }

                diffFormatter.format(diffEntry);
                final String gitDiff = outputStream.toString(StandardCharsets.UTF_8);
                final Result<PatchDiff, DiffError> patchDiff =
                        getBeforeFullFile(git, commit, diffEntry.getOldPath()).unwrap()
                        .bind(file -> createPatchDiff(
                                commitDiff,
                                diffEntry,
                                gitDiff,
                                file,
                                parseOptions).unwrap()
                        );

                patchDiff.ifSuccess(commitDiff::addPatchDiff);
                patchDiff.ifFailure(error -> result.second().add(error));
                outputStream.reset();
            }
        } catch (IOException e) {
            return CommitDiffResult.Failure(JGIT_ERROR, e.toString());
        }

        return new CommitDiffResult(result);
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
        final Pattern headerPattern = Pattern.compile(DIFF_HEADER_REGEX, Pattern.MULTILINE);
        final Matcher matcher = headerPattern.matcher(gitDiff);
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
        String[] beforeLines = beforeFile.split("(\\r\\n|\\r|\\n)", -1);
        String[] diffLines = gitDiff.split("(\\r\\n|\\r|\\n)");

        int beforeIndex = 0;

        List<String> fullDiffLines = new ArrayList<>();

        for (String diffLine : diffLines) {
            Pattern diffHunkPattern = Pattern.compile(DIFF_HUNK_REGEX);
            Matcher matcher = diffHunkPattern.matcher(diffLine);

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
        fullDiff = fullDiff.replaceAll(BOM_REGEX, "");

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
                return DiffResult.Failure(COULD_NOT_OBTAIN_FULLDIFF, "Could not obtain full diff of file " + filename + " before commit " + commit + "!");
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = git.getRepository().open(objectId);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            loader.copyTo(stream);
            return DiffResult.Success(stream.toString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return DiffResult.Failure(COULD_NOT_OBTAIN_FULLDIFF, "Could not obtain full diff of file " + filename + " before commit " + commit + "!");
        }
    }
}
