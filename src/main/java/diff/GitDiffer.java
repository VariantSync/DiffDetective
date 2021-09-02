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
import util.Yield;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
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
            Logger.warn("Commit {} does not have parents",
                    currentCommit.getId().abbreviate(7).name());
            return new CommitDiff(currentCommit);
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
        CommitDiff commitDiff = new CommitDiff(childCommit);

        // get TreeParsers
        CanonicalTreeParser currentTreeParser = new CanonicalTreeParser();
        CanonicalTreeParser prevTreeParser = new CanonicalTreeParser();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
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

        DiffTree diffTree = createDiffTree(diffString, true, true);

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
    public static DiffTree createDiffTree(String fullDiff,
                                    boolean collapseMultipleCodeLines,
                                    boolean ignoreEmptyLines) {
        String[] fullDiffLines = fullDiff.split(NEW_LINE_REGEX);

        List<DiffNode> codeNodes = new ArrayList<>();
        List<DiffNode> annotationNodes = new ArrayList<>();

        Stack<DiffNode> beforeStack = new Stack<>();
        Stack<DiffNode> afterStack = new Stack<>();

        DiffNode lastCode = null;
        boolean validDiff = true;

        MultilineMacro beforeMLMacro = null, afterMLMacro = null;

        final DiffNode root = DiffNode.createRoot();
        beforeStack.push(root);
        afterStack.push(root);

        for (int i = 0; i < fullDiffLines.length; i++) {
            String currentLine = fullDiffLines[i];
            final DiffNode.CodeType codeType = DiffNode.getCodeType(currentLine);
            final DiffNode.DiffType diffType = DiffNode.getDiffType(currentLine);

            if (ignoreEmptyLines && (currentLine.length() == 0
                    // TODO: Why substring(1) here? Because of + or - at the beginning of a line (i.e., when an empty
                    //       line was added or removed)?
                    || currentLine.substring(1).isEmpty())) {
                // discard empty lines
                continue;
            }

            // check if this is a multiline macro
            if (MultilineMacro.continuesMultilineDefinition(currentLine)) {
                // header
                if (codeType.isConditionalMacro()) {
                    if (diffType != DiffNode.DiffType.ADD) {
                        beforeMLMacro = new MultilineMacro(currentLine, i);
                    }
                    if (diffType != DiffNode.DiffType.REM) {
                        afterMLMacro = new MultilineMacro(currentLine, i);
                    }
                } else { // body
                    if (diffType != DiffNode.DiffType.ADD) {
                        beforeMLMacro.lines.add(currentLine);
                    }
                    if (diffType != DiffNode.DiffType.REM) {
                        afterMLMacro.lines.add(currentLine);
                    }
                }

                continue;
            } else {
                boolean inBeforeMLMacro = beforeMLMacro != null;
                boolean inAfterMLMacro = afterMLMacro != null;

                // check if last line of a multi macro
                if (inBeforeMLMacro || inAfterMLMacro) {
                    if (inBeforeMLMacro && inAfterMLMacro
                            && diffType == DiffNode.DiffType.NON
                            && beforeMLMacro.equals(afterMLMacro)) {
                        // we have one single end line for both multi line macros
                        beforeMLMacro.lines.add(currentLine);
                        beforeMLMacro.endLineInDiff = i;
                        DiffNode mlNode = beforeMLMacro.toDiffNode(beforeStack.peek(), afterStack.peek());

                        if (!pushNodeToStack(
                                mlNode,
                                beforeStack,
                                beforeMLMacro.getLineFrom())) {
                            validDiff = false;
                            break;
                        }
                        if (!pushNodeToStack(
                                mlNode,
                                afterStack,
                                afterMLMacro.getLineFrom())) {
                            validDiff = false;
                            break;
                        }

                        beforeMLMacro = null;
                        afterMLMacro = null;
                    } else {
                        if (inBeforeMLMacro && diffType != DiffNode.DiffType.ADD) {
                            beforeMLMacro.lines.add(currentLine);
                            beforeMLMacro.endLineInDiff = i;
                            DiffNode beforeMLNode = beforeMLMacro.toDiffNode(beforeStack.peek(), afterStack.peek());

                            if (!pushNodeToStack(
                                    beforeMLNode,
                                    beforeStack,
                                    beforeMLMacro.getLineFrom())) {
                                validDiff = false;
                                break;
                            }

                            beforeMLMacro = null;
                        }

                        if (afterMLMacro != null && diffType != DiffNode.DiffType.REM) {
                            afterMLMacro.lines.add(currentLine);
                            afterMLMacro.endLineInDiff = i;
                            DiffNode afterMLNode = afterMLMacro.toDiffNode(beforeStack.peek(), afterStack.peek());

                            if (!pushNodeToStack(
                                    afterMLNode,
                                    afterStack,
                                    afterMLMacro.getLineFrom())) {
                                validDiff = false;
                                break;
                            }

                            afterMLMacro = null;
                        }
                    }

                    continue;
                }
            }


            // This gets the code type and diff type of the current line and creates a node
            // Note that the node is not yet added to the diff tree
            DiffNode newNode = DiffNode.fromLine(currentLine, beforeStack.peek(), afterStack.peek());

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
                    if (!popIf(beforeStack)) {
                        Logger.warn("(before-) stack is empty!");
                        validDiff = false;
                        break;
                    }
                }
                if (!newNode.isRem()) {
                    // set corresponding line of now closed annotation
                    afterStack.peek().setToLine(i);

                    // pop the relevant stacks until an if node is popped
                    if (!popIf(afterStack)) {
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
                    if (!pushNodeToStack(newNode, beforeStack, i)) {
                        validDiff = false;
                        break;
                    }
                }
                if (!newNode.isRem()) {
                    if (!pushNodeToStack(newNode, afterStack, i)) {
                        validDiff = false;
                        break;
                    }
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
            return new DiffTree(root, codeNodes, annotationNodes);
        } else {
            return null;
        }
    }

    /**
     * Pops elements from the given stack until an if node is popped or the stack is empty.
     * @param stack The stack to pop the first if node from.
     * @return false if the stack is empty afterwards. Returns true otherwise (i.e., if an if code be popped).
     */
    private static boolean popIf(final Stack<DiffNode> stack) {
        // pop the relevant stacks until an if node is popped
        DiffNode popped;
        do {
            popped = stack.pop();
        } while (!popped.isIf() && !popped.isRoot());

        return !stack.isEmpty();
    }

    private static boolean pushNodeToStack(final DiffNode newNode, final Stack<DiffNode> stack, int currentLine) {
        if (newNode.isElif() || newNode.isElse()) {
            if (stack.size() == 1) {
                Logger.warn("#else or #elif without if!");
                return false;
            }

            // set corresponding line of now closed annotation
            stack.peek().setToLine(currentLine - 1);
        }

        stack.push(newNode);
        return true;
    }

    /**
     * Adds a DiffNode as a child to its parents
     *
     * @param diffNode The DiffNode to be added as a child to its parents
     */
    private static void addChildrenToParents(DiffNode diffNode) {
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
