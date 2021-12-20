package diff.difftree.parse;

import diff.DiffLineNumber;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.DiffType;
import diff.difftree.error.IllFormedAnnotationException;
import org.pmw.tinylog.Logger;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DiffTreeParser {
    public static final String NEW_LINE_REGEX = "(\\r\\n|\\r|\\n)";

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
        final String[] fullDiffLines = fullDiff.split(NEW_LINE_REGEX);

        final List<DiffNode> nodes = new ArrayList<>();
        final Stack<DiffNode> beforeStack = new Stack<>();
        final Stack<DiffNode> afterStack = new Stack<>();
        final DiffLineNumber lineNo = new DiffLineNumber(0, 0, 0);
        final DiffLineNumber lastLineNo = DiffLineNumber.Copy(lineNo);

        DiffNode lastCode = null;
        final AtomicBoolean error = new AtomicBoolean(false);
        final Consumer<String> errorHandler = m -> {
            Logger.warn(m);
            error.set(true);
        };

        final MultiLineMacroParser mlMacroParser = new MultiLineMacroParser();

        final DiffNode root = DiffNode.createRoot();
        beforeStack.push(root);
        afterStack.push(root);

        for (int i = 0; i < fullDiffLines.length; i++) {
            final String currentLine = fullDiffLines[i];
            final DiffType diffType = DiffType.ofDiffLine(currentLine);

            // count line numbers
            lastLineNo.set(lineNo);
            lineNo.inDiff = i + 1;
            diffType.matchBeforeAfter(() -> ++lineNo.beforeEdit, () -> ++lineNo.afterEdit);

            // Ignore line if it is empty.
            if (ignoreEmptyLines && (currentLine.isEmpty()
                    // substring(1) here because of diff symbol ('+', '-', ' ') at the beginning of a line.
                    || currentLine.substring(1).trim().isEmpty())) {
                // discard empty lines
                continue;
            }

            // check if this is a multiline macro
            final ParseResult isMLMacro;
            try {
                isMLMacro = mlMacroParser.consume(
                        lineNo, currentLine, beforeStack, afterStack, nodes);
            } catch (IllFormedAnnotationException e) {
                errorHandler.accept(e.toString());
                return null;
            }

            switch (isMLMacro.type()) {
                case Success: {
                    if (lastCode != null) {
                        lastCode = endCodeBlock(lastCode, lastLineNo);
                    }
                    // This line belongs to a multiline macro and was handled, so go to the next line.
                    continue;
                }
                case Error: {
                    errorHandler.accept(isMLMacro.message());
                    return null;
                }
                // line is not a mult-line macro so keep going (break the switch statement).
                case NotMyDuty: break;
            }

            // This gets the code type and diff type of the current line and creates a node
            // Note that the node is not yet added to the diff tree.
            final DiffNode newNode;
            try {
                newNode = DiffNode.fromDiffLine(currentLine);
            } catch (IllFormedAnnotationException e) {
                errorHandler.accept(e.toString());
                return null;
            }

            // collapse multiple code lines
            if (lastCode != null) {
                if (collapseMultipleCodeLines && newNode.isCode() && lastCode.diffType.equals(newNode.diffType)) {
                    lastCode.setLabel(lastCode.getLabel() + StringUtils.LINEBREAK + newNode.getLabel());
                    continue;
                } else {
                    lastCode = endCodeBlock(lastCode, lastLineNo);
                }
            }

            newNode.getFromLine().set(lineNo);
            if (!newNode.isEndif()) {
                newNode.addBelow(beforeStack.peek(), afterStack.peek());
                nodes.add(newNode);
            }

            if (newNode.isCode()) {
                lastCode = newNode;
            } else if (newNode.isEndif()) {
                diffType.matchBeforeAfter(beforeStack, afterStack,
                        stack -> {
                            // Set corresponding line of now closed annotation.
                            // The last block is the first one on the stack.
                            endMacroBlock(stack.peek(), lastLineNo, diffType);

                            // Pop the relevant stacks until an IF node is popped. If there were ELSEs or ELIFs between
                            // an IF and an ENDIF, they were placed on the stack and have to be popped now.
                            popIf(stack);

                            if (stack.isEmpty()) {
                                errorHandler.accept("ENDIF without IF at line " + currentLine + "!");
                            }
                        });
                if (error.get()) { return null; }
            } else {
                // newNode is if, elif or else
                // push the node to the relevant stacks
                diffType.matchBeforeAfter(beforeStack, afterStack, stack ->
                        pushNodeToStack(newNode, stack, lastLineNo).onError(errorHandler)
                );
                if (error.get()) { return null; }
            }
        }

        if (beforeStack.size() > 1 || afterStack.size() > 1) {
            errorHandler.accept("Not all annotations closed!");
            return null;
        }

        if (lastCode != null) {
            lastCode = endCodeBlock(lastCode, lineNo);
        }

        endCodeBlock(root, lineNo);

        // Invalidate line numbers according to edits.
        // E.g. if a node was added, it had no line number before the edit.
        for (final DiffNode node : nodes) {
            node.getFromLine().as(node.diffType);
            node.getToLine().as(node.diffType);
        }

        return new DiffTree(root);
    }

    static ParseResult pushNodeToStack(
            final DiffNode newNode,
            final Stack<DiffNode> stack,
            final DiffLineNumber lastLineNo) {
        if (newNode.isElif() || newNode.isElse()) {
            if (stack.size() == 1) {
                return ParseResult.ERROR("#else or #elif without if!");
            }

            // set corresponding line of now closed annotation
            endMacroBlock(stack.peek(), lastLineNo, newNode.diffType);
        }

        stack.push(newNode);
        return ParseResult.SUCCESS;
    }

    private static DiffNode endCodeBlock(final DiffNode block, final DiffLineNumber lastLineNo) {
        block.getToLine().set(lastLineNo).add(1);
        return null;
    }

    private static void endMacroBlock(final DiffNode block, final DiffLineNumber lastLineNo, final DiffType diffTypeOfNewBlock) {
        // Add 1 because the end line is exclusive, so we have to point one behind the last line we found.
        final DiffLineNumber to = block.getToLine();
        diffTypeOfNewBlock.matchBeforeAfter(
                () -> to.beforeEdit = lastLineNo.beforeEdit + 1,
                () -> to.afterEdit = lastLineNo.afterEdit + 1);
        // Take the highest value ever set as we want to include all lines that are somehow affected by this block.
        to.inDiff = Math.max(to.inDiff, lastLineNo.inDiff + 1);
    }

    public static DiffNode popIf(final Stack<DiffNode> stack) {
        DiffNode popped;
        do {
            // Don't update line numbers of popped nodes here as this already happened.
            popped = stack.pop();
        } while (!popped.isIf() && !popped.isRoot());
        return popped;
    }
    
}
