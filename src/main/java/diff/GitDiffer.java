package diff;

import diff.data.*;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
    public static final String NEW_LINE_REGEX = "(\\r\\n|\\r|\\n)";
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
                CommitDiff commitDiff = createCommitDiff(c);
                gitDiff.addCommitDiff(commitDiff);
            }
            gitDiff.setCommitAmount(commitAmount);
        } catch (IOException e) {
            Logger.warn("Could not get diffs for git repository {}", git.toString());
            return null;
        }
        return gitDiff;
    }

    /**
     * Creates a CommitDiff from a given commit.
     * For this, the git diff is retrieved using JGit.
     * For each file in the diff, a PatchDiff is created.
     *
     * @param currentCommit The commit from which to create a CommitDiff
     * @return The CommitDiff of the given commit
     * @throws IOException When problems with the git repository occur
     */
    private CommitDiff createCommitDiff(RevCommit currentCommit) throws IOException {
        CommitDiff commitDiff = new CommitDiff(currentCommit);

        if (currentCommit.getParentCount() == 0) {
            Logger.warn("Commit {} does not have parents",
                    currentCommit.getId().abbreviate(7).name());
            return commitDiff;
        }

        RevCommit prevCommit = currentCommit.getParent(0);

        // get TreeParsers
        CanonicalTreeParser currentTreeParser = new CanonicalTreeParser();
        CanonicalTreeParser prevTreeParser = new CanonicalTreeParser();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            currentTreeParser.reset(reader, currentCommit.getTree());
            prevTreeParser.reset(reader, prevCommit.getTree());
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
                String gitDiff = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

                Pattern headerPattern = Pattern.compile(DIFF_HEADER_REGEX, Pattern.MULTILINE);
                Matcher matcher = headerPattern.matcher(gitDiff);
                if (matcher.find()) {
                    gitDiff = gitDiff.substring(matcher.end() + 1);
                }

                String beforeFullFile = getBeforeFullFile(prevCommit, diffEntry.getOldPath());

                commitDiff.addPatchDiff(createPatchDiff(commitDiff, diffEntry, gitDiff,
                        beforeFullFile));
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
     * @return The PatchDiff of the given DiffEntry
     */
    private PatchDiff createPatchDiff(CommitDiff commitDiff, DiffEntry diffEntry, String gitDiff,
                                      String beforeFullFile) {
        String fullDiff = getFullDiff(beforeFullFile, gitDiff);

        // this could be used to combine multiple lines that end with "\" but it does not work
        // reliably as it does not detect when only one of the lines was changed
//        String diffString = combineMultiLines(fullDiff);
        String diffString = fullDiff;

        DiffTree diffTree = createDiffTree(diffString, true, true);

        if (diffTree == null) {
            Logger.warn("Something went wrong parsing patch for file {} at commit {}!",
                    diffEntry.getOldPath(), commitDiff.getAbbreviatedCommitHash());
        }

        if (saveMemory) {
            // not storing the full diff reduces memory usage by around 40-50%
            return new PatchDiff(commitDiff, diffEntry, "", diffTree);
        } else {
            return new PatchDiff(commitDiff, diffEntry, fullDiff, diffTree);
        }
    }

    private String combineMultiLines(String fullDiff) {
        String MULTI_LINE_BREAK_REGEX = "\\\\$\\s*^[ +-]\\s*";
        Pattern pattern = Pattern.compile(MULTI_LINE_BREAK_REGEX, Pattern.MULTILINE);
        return pattern.matcher(fullDiff).replaceAll("");
    }

    /**
     * Implementation of the diff tree algorithm.
     * This implementation has options to collapse multiple code lines into one node and to
     * discard empty lines.
     * The implementation also checks for faulty git diffs.
     *
     * @param fullDiff                  The full diff of a patch
     * @param collapseMultipleCodeLines Whether multiple consecutive code lines should be
     *                                  collapsed into a single code node
     * @param ignoreEmptyLines          Whether empty lines (no matter if they are added removed
     *                                  or remained unchanged) should be ignored
     * @return The DiffTree created from the given git diff
     */
    private DiffTree createDiffTree(String fullDiff,
                                    boolean collapseMultipleCodeLines,
                                    boolean ignoreEmptyLines) {
        String[] fullDiffLines = fullDiff.split(NEW_LINE_REGEX);

        List<DiffNode> codeNodes = new ArrayList<>();
        List<DiffNode> annotationNodes = new ArrayList<>();

        Stack<DiffNode> beforeStack = new Stack<>();
        Stack<DiffNode> afterStack = new Stack<>();


        DiffNode lastCode = null;
        boolean validDiff = true;

        DiffNode root = DiffNode.createRoot();
        beforeStack.push(root);
        afterStack.push(root);

        for (int i = 0; i < fullDiffLines.length; i++) {
            if (ignoreEmptyLines && (fullDiffLines[i].length() == 0
                    || fullDiffLines[i].substring(1).isEmpty())) {
                // discard empty lines
                continue;
            }

            // This gets the code type and diff type of the current line and creates a node
            // Note that the node is not yet added to the diff tree
            DiffNode newNode = DiffNode.fromLine(fullDiffLines[i], beforeStack.peek(),
                    afterStack.peek());

            // collapse multiple code lines
            if (collapseMultipleCodeLines && lastCode != null && newNode.isCode()
                    && lastCode.diffType.equals(newNode.diffType)) {
                continue;
            } else if (lastCode != null) {
                lastCode.setToLine(i);
            }

            if (newNode.isCode()) {
                newNode.setFromLine(i);
                codeNodes.add(newNode);
                addChildrenToParents(newNode);
                lastCode = newNode;

            } else if (newNode.isEndif()) {
                lastCode = null;
                if (!newNode.isAdd()) {

                    // set corresponding line of now closed annotation
                    beforeStack.peek().setToLine(i);

                    // pop the relevant stacks until an if node is popped
                    DiffNode popped;
                    do {
                        popped = beforeStack.pop();
                    } while (!popped.isIf() && !popped.isRoot());

                    if (beforeStack.isEmpty()) {
                        Logger.warn("(before-) stack is empty!");
                        validDiff = false;
                        break;
                    }
                }
                if (!newNode.isRem()) {

                    // set corresponding line of now closed annotation
                    afterStack.peek().setToLine(i);

                    // pop the relevant stacks until an if node is popped
                    DiffNode popped;
                    do {
                        popped = afterStack.pop();
                    } while (!popped.isIf() && !popped.isRoot());

                    if (afterStack.isEmpty()) {
                        Logger.warn("(after-) stack is empty!");
                        validDiff = false;
                        break;
                    }
                }

            } else {
                // newNode is if, elif or else
                lastCode = null;

                // push the node to the relevant stacks
                if (!newNode.isAdd()) {
                    if ((newNode.isElif() || newNode.isElse()) && beforeStack.size() == 1) {
                        Logger.warn("#else or #elif without if!");
                        validDiff = false;
                        break;
                    }

                    // set corresponding line of now closed annotation
                    if ((newNode.isElif() || newNode.isElse())) {
                        beforeStack.peek().setToLine(i - 1);
                    }
                    beforeStack.push(newNode);
                }
                if (!newNode.isRem()) {
                    if ((newNode.isElif() || newNode.isElse()) && afterStack.size() == 1) {
                        Logger.warn("#else or #elif without if!");
                        validDiff = false;
                        break;
                    }

                    // set corresponding line of now closed annotation
                    if ((newNode.isElif() || newNode.isElse())) {
                        afterStack.peek().setToLine(i - 1);
                    }
                    afterStack.push(newNode);
                }

                newNode.setFromLine(i);
                annotationNodes.add(newNode);
                addChildrenToParents(newNode);
            }
        }

        if (lastCode != null) {
            lastCode.setToLine(fullDiffLines.length);
        }

        if (beforeStack.size() > 1 || afterStack.size() > 1) {
            Logger.warn("Not all annotations closed!");
            validDiff = false;
        }

        if (validDiff) {
            return new DiffTree(codeNodes, annotationNodes);
        } else {
            return null;
        }
    }

    /**
     * Adds a DiffNode as a child to its parents
     *
     * @param diffNode The DiffNode to be added as a child to its parents
     */
    private void addChildrenToParents(DiffNode diffNode) {
        if (diffNode.getAfterParent() != null) {
            diffNode.getAfterParent().addChild(diffNode);
        }
        if (diffNode.getBeforeParent() != null) {
            diffNode.getBeforeParent().addChild(diffNode);
        }
    }

    /**
     * Creates a full git diff from a file before the change and the git diff containing only the
     * changed lines
     *
     * @param beforeFile The full file before the change
     * @param gitDiff    The git diff containing only the changed lines
     * @return A full git diff containing the complete file and all changes
     */
    private String getFullDiff(String beforeFile, String gitDiff) {
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
    private String getBeforeFullFile(RevCommit commit, String filename) throws IOException {
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
            return new String(stream.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
