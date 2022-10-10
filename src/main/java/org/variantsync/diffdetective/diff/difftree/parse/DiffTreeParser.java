package org.variantsync.diffdetective.diff.difftree.parse;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffResult;
import org.variantsync.diffdetective.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Parser that parses {@link DiffTree}s from text-based diffs.
 */
public class DiffTreeParser {
    /**
     * The same as {@link DiffTreeParser#createDiffTree(BufferedReader, boolean, boolean, DiffNodeParser)}
     * but with the diff given as a single string with linebreaks instead of a BufferedReader.
     * Rethrows IOExceptions as Assertion errors (that do not have to be catched).
     */
    public static DiffResult<DiffTree> createDiffTree(
            String fullDiff,
            boolean collapseMultipleCodeLines,
            boolean ignoreEmptyLines,
            DiffNodeParser nodeParser)
    {
        try {
            return createDiffTree(new BufferedReader(new StringReader(fullDiff)), collapseMultipleCodeLines, ignoreEmptyLines, nodeParser);
        } catch (IOException e) {
            throw new AssertionError("No actual IO should be performed, because only a StringReader is used");
        }
    }

    /**
     * Default parsing method for DiffTrees from diffs.
     * This implementation has options to collapse multiple code lines into one node and to
     * discard empty lines.
     * This parsing algorithm is described in detail in Sören Viegener's bachelor's thesis.
     *
     * @param fullDiff                  The full diff of a patch obtained from a buffered reader.
     * @param collapseMultipleCodeLines Whether multiple consecutive code lines with the same diff type
     *                                  should be collapsed into a single artifact node.
     * @param ignoreEmptyLines          Whether empty lines (no matter if they are added removed
     *                                  or remained unchanged) should be ignored.
     * @param nodeParser                The parser to parse individual lines in the diff to DiffNodes.
     * @return A parsed DiffTree upon success or an error indicating why parsing failed.
     * @throws IOException when reading the given BufferedReader fails.
     */
    public static DiffResult<DiffTree> createDiffTree(
            BufferedReader fullDiff,
            boolean collapseMultipleCodeLines,
            boolean ignoreEmptyLines,
            DiffNodeParser nodeParser) throws IOException
    {
        final Stack<DiffNode> beforeStack = new Stack<>();
        final Stack<DiffNode> afterStack = new Stack<>();
        DiffLineNumber lineNo = new DiffLineNumber(0, 0, 0);
        DiffLineNumber lastLineNo = lineNo;

        DiffNode lastArtifact = null;
        final AtomicReference<DiffResult<DiffTree>> error = new AtomicReference<>();
        final BiConsumer<DiffError, String> errorPropagation = (errType, message) -> {
            if (error.get() == null) {
                error.set(DiffResult.Failure(errType, message));
            }
        };

        final MultiLineMacroParser mlMacroParser = new MultiLineMacroParser(nodeParser);

        final DiffNode root = DiffNode.createRoot();
        beforeStack.push(root);
        afterStack.push(root);

        String currentLine;
        for (int i = 0; (currentLine = fullDiff.readLine()) != null; i++) {
            final DiffType diffType = DiffType.ofDiffLine(currentLine);

            // count line numbers
            lastLineNo = lineNo;
            lineNo = lineNo.add(1, diffType);

            // Ignore line if it is empty.
            if (ignoreEmptyLines && (currentLine.isEmpty()
                    // substring(1) here because of diff symbol ('+', '-', ' ') at the beginning of a line.
                    || currentLine.substring(1).isBlank())) {
                // discard empty lines
                continue;
            }

            // check if this is a multiline macro
            final ParseResult isMLMacro;
            try {
                isMLMacro = mlMacroParser.consume(lineNo, currentLine, beforeStack, afterStack);
            } catch (IllFormedAnnotationException e) {
                return DiffResult.Failure(e);
            }

            switch (isMLMacro.type()) {
                case Success: {
                    if (lastArtifact != null) {
                        lastArtifact = endCodeBlock(lastArtifact, lastLineNo);
                    }
                    // This line belongs to a multiline macro and was handled, so go to the next line.
                    continue;
                }
                case Error: {
                    isMLMacro.onError(errorPropagation);
                    return error.get();
                }
                // line is not a mult-line macro so keep going (break the switch statement).
                case NotMyDuty: break;
            }

            if ("endif".equals(MultiLineMacroParser.conditionalMacroName(currentLine))) {
                if (lastArtifact != null) {
                    lastArtifact = endCodeBlock(lastArtifact, lastLineNo);
                }

                final String currentLineFinal = currentLine;
                final DiffLineNumber lastLineNoFinal = lastLineNo;
                diffType.matchBeforeAfter(beforeStack, afterStack,
                        stack -> {
                            // Set corresponding line of now closed annotation.
                            // The last block is the first one on the stack.
                            endMacroBlock(stack.peek(), lastLineNoFinal, diffType);

                            // Pop the relevant stacks until an IF node is popped. If there were ELSEs or ELIFs between
                            // an IF and an ENDIF, they were placed on the stack and have to be popped now.
                            popIf(stack);

                            if (stack.isEmpty()) {
                                errorPropagation.accept(DiffError.ENDIF_WITHOUT_IF, "ENDIF without IF at line \"" + currentLineFinal + "\"!");
                            }
                        });
                if (error.get() != null) { return error.get(); }
            } else {
                // This gets the node type and diff type of the current line and creates a node
                // Note that the node is not yet added to the diff tree.
                final DiffNode newNode;
                try {
                    newNode = nodeParser.fromDiffLine(currentLine);
                } catch (IllFormedAnnotationException e) {
                    return DiffResult.Failure(e);
                }

                // collapse multiple code lines
                if (lastArtifact != null) {
                    if (collapseMultipleCodeLines && newNode.isArtifact() && lastArtifact.diffType.equals(newNode.diffType)) {
                        lastArtifact.addLines(newNode.getLines());
                        continue;
                    } else {
                        lastArtifact = endCodeBlock(lastArtifact, lastLineNo);
                    }
                }

                newNode.setFromLine(lineNo);
                newNode.addBelow(beforeStack.peek(), afterStack.peek());

                if (newNode.isArtifact()) {
                    lastArtifact = newNode;
                } else {
                    // newNode is if, elif or else
                    // push the node to the relevant stacks
                    final DiffLineNumber lastLineNoFinal = lastLineNo;
                    diffType.matchBeforeAfter(beforeStack, afterStack, stack ->
                            pushNodeToStack(newNode, stack, lastLineNoFinal).onError(errorPropagation)
                    );
                    if (error.get() != null) { return error.get(); }
                }
            }
        }

        if (beforeStack.size() > 1 || afterStack.size() > 1) {
            return DiffResult.Failure(DiffError.NOT_ALL_ANNOTATIONS_CLOSED);
        }

        if (lastArtifact != null) {
            lastArtifact = endCodeBlock(lastArtifact, lineNo);
        }

        return DiffResult.Success(new DiffTree(root));
    }

    /**
     * Push the given node to the given stack including some sanity checks.
     * This method also ensures that annotation blocks are closed correctly,
     * by setting ending line numbers.
     * @param newNode The node to push to the given parsing stack.
     * @param stack Describes current the nesting depth of feature annotations.
     * @param lastLineNo The current line number from which's line the given node was parsed.
     * @return Either success or an error in case a sanity check failed.
     */
    static ParseResult pushNodeToStack(
            final DiffNode newNode,
            final Stack<DiffNode> stack,
            final DiffLineNumber lastLineNo) {
        if (newNode.isElif() || newNode.isElse()) {
            if (stack.size() == 1) {
                return ParseResult.ERROR(DiffError.ELSE_OR_ELIF_WITHOUT_IF);
            }

            if (stack.peek().isElse()) {
                return ParseResult.ERROR(DiffError.ELSE_AFTER_ELSE);
            }

            // set corresponding line of now closed annotation
            endMacroBlock(stack.peek(), lastLineNo, newNode.diffType);
        }

        stack.push(newNode);
        return ParseResult.SUCCESS;
    }

    /**
     * Ends a given code block by setting its ending line number correctly.
     * @param block The code block to end.
     * @param lastLineNo The line number of the last parsed line.
     *                   This should be the line number of the last line in the diff
     *                   that is part of the given block.
     * @return null
     */
    private static DiffNode endCodeBlock(final DiffNode block, final DiffLineNumber lastLineNo) {
        block.setToLine(lastLineNo.add(1));
        return null;
    }

    /**
     * Ends a given macro block by setting its ending line number correctly.
     * @param block The macro block to end.
     * @param lastLineNo The last line number in the diff that is part of the given block.
     * @param diffTypeOfNewBlock The diff type of the upcoming block. This type determines
     *                           at which times the given block ends.
     */
    private static void endMacroBlock(final DiffNode block, final DiffLineNumber lastLineNo, final DiffType diffTypeOfNewBlock) {
        // Add 1 because the end line is exclusive, so we have to point one behind the last line we found.
        final DiffLineNumber to = lastLineNo.add(1, diffTypeOfNewBlock);

        block.setToLine(new DiffLineNumber(
            // Take the highest value ever set as we want to include all lines that are somehow affected by this block.
            Math.max(block.getToLine().inDiff(), to.inDiff()),
            diffTypeOfNewBlock == DiffType.ADD ? block.getToLine().beforeEdit() : to.beforeEdit(),
            diffTypeOfNewBlock == DiffType.REM ? block.getToLine().afterEdit() : to.afterEdit()
        ));
    }

    /**
     * Pop nodes from the given annotation nesting stack until an If or the root was popped.
     * Used to exist if-elif-else chains.
     * @param stack The stack representing the current nesting of annotations.
     * @return The last popped node. This is either an IF node or the root.
     */
    public static DiffNode popIf(final Stack<DiffNode> stack) {
        DiffNode popped;
        do {
            // Don't update line numbers of popped nodes here as this already happened.
            popped = stack.pop();
        } while (!popped.isIf());
        return popped;
    }

    /**
     * Parses the given commit of the given repository.
     * @param repo The repository from which a commit should be parsed.
     * @param commitHash Hash of the commit to parse.
     * @return A CommitDiff describing edits to variability introduced by the given commit relative to its first parent commit.
     * @throws IOException when an error occurred.
     */
    public static CommitDiff parseCommit(Repository repo, String commitHash) throws IOException {
        final Git git = repo.getGitRepo().run();
        Assert.assertNotNull(git);
        final RevWalk revWalk = new RevWalk(git.getRepository());
        final RevCommit childCommit = revWalk.parseCommit(ObjectId.fromString(commitHash));
        final RevCommit parentCommit = revWalk.parseCommit(childCommit.getParent(0).getId());

        final CommitDiff commitDiff =
                GitDiffer.createCommitDiff(
                                git,
                                repo.getDiffFilter(),
                                parentCommit,
                                childCommit,
                                repo.getParseOptions())
                        .diff().orElseThrow();

        revWalk.close();
        return commitDiff;
    }

    /**
     * Parses the given patch of the given repository.
     * @param repo The repository from which a patch should be parsed.
     * @param file The file that was edited by the patch.
     * @param commitHash The hash of the commit in which the patch was made.
     * @return A PatchDiff describing edits to variability introduced by the given patch relative to the corresponding commit's first parent commit.
     * @throws IOException when an error occurred.
     * @throws AssertionError when no such patch exists.
     */
    public static PatchDiff parsePatch(Repository repo, String file, String commitHash) throws IOException {
        final CommitDiff commitDiff = parseCommit(repo, commitHash);

        for (final PatchDiff pd : commitDiff.getPatchDiffs()) {
            if (file.equals(pd.getFileName())) {
                return pd;
            }
        }

        Assert.fail("Did not find file \"" + file + "\" in commit " + commitHash + "!");
        return null;
    }
}
