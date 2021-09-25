package diff;

import diff.difftree.DiffTree;
import diff.difftree.parse.DiffTreeParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.pmw.tinylog.Logger;
import util.Yield;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class creates a GitDiff-object from a git repository (Git-object).
 * <p>
 * The commits from the repository are first filtered using the given DiffFilter.
 * Then a CommitDiff is created from each commit.
 * File changes in each commit are then filtered using the given DiffFilter.
 * Then a PatchDiff is created from each file change.
 * Each PatchDiff contains the DiffTree of its patch.
 *
 * @author Soeren Viegener
 */
public class GitDiffer {
    public static final String BOM_REGEX = "\\x{FEFF}";
    public static final String DIFF_HUNK_REGEX = "^@@\\s-(\\d+).*\\+(\\d+).*@@$";
    public static final String DIFF_HEADER_REGEX = "^\\+\\+\\+.*$";
    public static final String NO_NEW_LINE = "\\ No newline at end of file";

    private final Git git;
    private final DiffFilter diffFilter;
    private final boolean saveMemory;

    public GitDiffer(Git git, DiffFilter diffFilter, boolean saveMemory) {
        this.git = git;
        this.diffFilter = diffFilter;
        this.saveMemory = saveMemory;
    }

    /**
     * Creates a GitDiff object.
     * For this, each commit is iterated to create CommitDiffs
     *
     * @return The GitDiff object created from the Git object of this GitDiffer
     */
    public GitDiff createGitDiff() {
        GitDiff gitDiff = new GitDiff();

        Iterable<RevCommit> commitsIterable;
        try {
            commitsIterable = git.log().call();
        } catch (GitAPIException e) {
            Logger.warn("Could not get log for git repository {}", git.toString());
            return null;
        }
        try {
            // we specifically count the commits here because the amount of unfiltered commits is
            // otherwise lost
            int commitAmount = 0;
            for (RevCommit c : commitsIterable) {
                commitAmount++;
                if (!diffFilter.filter(c)) {
                    continue;
                }
                CommitDiff commitDiff = createCommitDiffFromFirstParent(git, diffFilter, c, !saveMemory);
                gitDiff.addCommitDiff(commitDiff);
            }
            gitDiff.setCommitAmount(commitAmount);
        } catch (IOException e) {
            Logger.warn("Could not get diffs for git repository {}", git.toString());
            return null;
        }
        return gitDiff;
    }

    public Yield<CommitDiff> yieldGitDiff() {
        final Iterable<RevCommit> commitsIterable;
        try {
            commitsIterable = git.log().call();
        } catch (GitAPIException e) {
            Logger.warn("Could not get log for git repository {}", git.toString());
            return null;
        }

        final Iterator<RevCommit> commitsIterator = commitsIterable.iterator();
        return new Yield<>(
                () -> {
                    RevCommit c = commitsIterator.next();
                    while (c != null && !diffFilter.filter(c)) {
                        c = commitsIterator.next();
                    }
                    try {
                        return c == null ? null : createCommitDiffFromFirstParent(git, diffFilter, c, !saveMemory);
                    } catch (IOException exception) {
                        Logger.error(exception);
                        return null;
                    }
                }
        );
    }

    /**
     * Creates a CommitDiff from a given commit.
     * For this, the git diff is retrieved using JGit.
     * For each file in the diff, a PatchDiff is created.
     *
     * @param git The git repo which the commit stems from.
     * @param currentCommit The commit from which to create a CommitDiff
     * @param keepFullDiffs  If true, the PatchDiff will contain the full diff as a string. Set to false if you want to
     *                       reduce memory consumption.
     * @return The CommitDiff of the given commit
     * @throws IOException When problems with the git repository occur
     */
    public static CommitDiff createCommitDiffFromFirstParent(
            Git git,
            DiffFilter diffFilter,
            RevCommit currentCommit,
            boolean keepFullDiffs) throws IOException {
        if (currentCommit.getParentCount() == 0) {
            throw new IOException("Commit " + currentCommit.getId().getName() + " does not have parents");
        }

        return createCommitDiff(git, diffFilter, currentCommit.getParent(0), currentCommit, keepFullDiffs);
    }

    /**
     * Creates a CommitDiff from a given commit.
     * For this, the git diff is retrieved using JGit.
     * For each file in the diff, a PatchDiff is created.
     *
     * @param git The git repo which the commit stems from.
     * @param keepFullDiffs  If true, the PatchDiff will contain the full diff as a string. Set to false if you want to
     *                       reduce memory consumption.
     * @return The CommitDiff of the given commit
     * @throws IOException When problems with the git repository occur
     */
    public static CommitDiff createCommitDiff(
            Git git,
            DiffFilter diffFilter,
            RevCommit parentCommit,
            RevCommit childCommit,
            boolean keepFullDiffs) throws IOException {
        CommitDiff commitDiff = new CommitDiff(childCommit, parentCommit);

        // get TreeParsers
        CanonicalTreeParser currentTreeParser = new CanonicalTreeParser();
        CanonicalTreeParser prevTreeParser = new CanonicalTreeParser();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            if (childCommit.getTree() == null) {
                throw new RuntimeException("Could not obtain RevTree from child commit " + childCommit.getId());
            }
            if (parentCommit.getTree() == null) {
                throw new RuntimeException("Could not obtain RevTree from parent commit " + parentCommit.getId());
            }
            currentTreeParser.reset(reader, childCommit.getTree());
            prevTreeParser.reset(reader, parentCommit.getTree());
        }


        // get PatchDiffs
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DiffFormatter diffFormatter = new DiffFormatter(outputStream)) {
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setDetectRenames(true);
            diffFormatter.getRenameDetector().setRenameScore(50);

            List<DiffEntry> entries = diffFormatter.scan(prevTreeParser, currentTreeParser);
            for (DiffEntry diffEntry : entries) {
                if (!diffFilter.filter(diffEntry)) {
                    continue;
                }

                diffFormatter.format(diffEntry);
                String gitDiff = outputStream.toString(StandardCharsets.UTF_8);

                Pattern headerPattern = Pattern.compile(DIFF_HEADER_REGEX, Pattern.MULTILINE);
                Matcher matcher = headerPattern.matcher(gitDiff);
                if (matcher.find()) {
                    gitDiff = gitDiff.substring(matcher.end() + 1);
                }

                String beforeFullFile = getBeforeFullFile(git, parentCommit, diffEntry.getOldPath());

                commitDiff.addPatchDiff(createPatchDiff(
                        commitDiff,
                        diffEntry,
                        gitDiff,
                        beforeFullFile,
                        keepFullDiffs));
                outputStream.reset();
            }
        }
        return commitDiff;
    }

    /**
     * Creates a PatchDiff from a given DiffEntry of a commit
     *
     * @param commitDiff     The CommitDiff the created PatchDiff belongs to
     * @param diffEntry      The DiffEntry of the file that was changed in the commit
     * @param gitDiff        The git diff of the file that was changed
     * @param beforeFullFile The full file before the change
     * @param keepFullDiffs  If true, the PatchDiff will contain the full diff as a string. Set to false if you want to
     *                       reduce memory consumption.
     * @return The PatchDiff of the given DiffEntry
     */
    private static PatchDiff createPatchDiff(
            CommitDiff commitDiff,
            DiffEntry diffEntry,
            String gitDiff,
            String beforeFullFile,
            boolean keepFullDiffs) {
        String fullDiff = getFullDiff(beforeFullFile, gitDiff);

        // this could be used to combine multiple lines that end with "\" but it does not work
        // reliably as it does not detect when only one of the lines was changed
//        String diffString = combineMultiLines(fullDiff);
        String diffString = fullDiff;

        DiffTree diffTree = DiffTreeParser.createDiffTree(diffString, true, true);

        if (diffTree == null) {
            Logger.warn("Something went wrong parsing patch for file {} at commit {}!",
                    diffEntry.getOldPath(), commitDiff.getAbbreviatedCommitHash());
        }

        if (keepFullDiffs) {
            return new PatchDiff(commitDiff, diffEntry, fullDiff, diffTree);
        } else {
            // not storing the full diff reduces memory usage by around 40-50%
            return new PatchDiff(commitDiff, diffEntry, "", diffTree);
        }
    }

    private static String combineMultiLines(String fullDiff) {
        String MULTI_LINE_BREAK_REGEX = "\\\\$\\s*^[ +-]\\s*";
        Pattern pattern = Pattern.compile(MULTI_LINE_BREAK_REGEX, Pattern.MULTILINE);
        return pattern.matcher(fullDiff).replaceAll("");
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
     * @throws IOException When accessing the file failed
     */
    public static String getBeforeFullFile(Git git, RevCommit commit, String filename) throws IOException {
        RevTree tree = commit.getTree();

        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filename));
            if (!treeWalk.next()) {
                return null;
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = git.getRepository().open(objectId);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            loader.copyTo(stream);
            return stream.toString(StandardCharsets.UTF_8);
        }
    }
}
