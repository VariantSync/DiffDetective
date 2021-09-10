package diff.difftree.parse;

import diff.DiffLineNumber;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.DiffType;
import org.pmw.tinylog.Logger;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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

        final List<DiffNode> codeNodes = new ArrayList<>();
        final List<DiffNode> annotationNodes = new ArrayList<>();

        final Stack<DiffNode> beforeStack = new Stack<>();
        final Stack<DiffNode> afterStack = new Stack<>();
        final DiffLineNumber lineNo = new DiffLineNumber(0, 0, 0);
        final DiffLineNumber lastLineNo = DiffLineNumber.Copy(lineNo);

        DiffNode lastCode = null;
        final Consumer<String> errorHandler = Logger::warn;

        final MultiLineMacroParser mlMacroParser = new MultiLineMacroParser();

        final DiffNode root = DiffNode.createRoot();
        beforeStack.push(root);
        afterStack.push(root);

        for (int i = 0; i < fullDiffLines.length; i++) {
            final String currentLine = fullDiffLines[i];
            final DiffType currentLinesDiffType = DiffType.ofDiffLine(currentLine);

            // count line numbers
            lastLineNo.set(lineNo);
            lineNo.inDiff = i + 1;
            if (currentLinesDiffType != DiffType.ADD) {
                ++lineNo.beforeEdit;
            }
            if (currentLinesDiffType != DiffType.REM) {
                ++lineNo.afterEdit;
            }

            // Ignore line if it is empty.
            if (ignoreEmptyLines && (currentLine.isEmpty()
                    // substring(1) here because of diff symbol ('+', '-', ' ') at the beginning of a line.
                    || currentLine.substring(1).trim().isEmpty())) {
                // discard empty lines
                continue;
            }

            // check if this is a multiline macro
            final ParseResult isMLMacro = mlMacroParser.consume(
                    lineNo,
                    currentLine,
                    beforeStack, afterStack, annotationNodes);
            switch (isMLMacro.type()) {
                case Success: {
                    if (lastCode != null) {
                        lastCode = endBlock(lastCode, lastLineNo);
                    }
                    // This line belongs to a multiline macro and was handled, so go to the next line.
                    continue;
                }
                case Error: {
                    errorHandler.accept(isMLMacro.message());
                    return null;
                }
                case NotMyDuty: break;
            }

            // This gets the code type and diff type of the current line and creates a node
            // Note that the node is not yet added to the diff tree
            final DiffNode newNode = DiffNode.fromDiffLine(currentLine, beforeStack.peek(), afterStack.peek());
            newNode.getFromLine().set(lineNo);

            if (lastCode != null) {
                // collapse multiple code lines
                if (collapseMultipleCodeLines && newNode.isCode() && lastCode.diffType.equals(newNode.diffType)) {
                    lastCode.setText(lastCode.getText() + StringUtils.LINEBREAK + newNode.getText());
                    continue;
                } else {
                    lastCode = endBlock(lastCode, lastLineNo);
                }
            }

            if (newNode.isCode()) {
                lastCode = newNode;
                codeNodes.add(newNode);
                addChildrenToParents(newNode);
            } else if (newNode.isEndif()) {
                if (!newNode.isAdd()) {
                    // set corresponding line of now closed annotation
                    endBlock(beforeStack.peek(), lastLineNo);

                    // pop the relevant stacks until an if node is popped
                    if (!popIf(beforeStack, lineNo)) {
                        errorHandler.accept("(before-) stack is empty!");
                        return null;
                    }
                }
                if (!newNode.isRem()) {
                    // set corresponding line of now closed annotation
                    endBlock(afterStack.peek(), lastLineNo);

                    // pop the relevant stacks until an if node is popped
                    if (!popIf(afterStack, lineNo)) {
                        errorHandler.accept("(after-) stack is empty!");
                        return null;
                    }
                }
            } else {
                // newNode is if, elif or else
                // push the node to the relevant stacks
                if (!newNode.isAdd()) {
                    if (pushNodeToStack(newNode, beforeStack, lastLineNo).onError(errorHandler)) {
                        return null;
                    }
                }
                if (!newNode.isRem()) {
                    if (pushNodeToStack(newNode, afterStack, lastLineNo).onError(errorHandler)) {
                        return null;
                    }
                }

                annotationNodes.add(newNode);
                addChildrenToParents(newNode);
            }
        }

        if (beforeStack.size() > 1 || afterStack.size() > 1) {
            errorHandler.accept("Not all annotations closed!");
            return null;
        }

        if (lastCode != null) {
            lastCode = endBlock(lastCode, lineNo);
        }

        endBlock(root, lineNo);

        // Invalidate line numbers according to edits.
        // E.g. if a node was added, it had no line number before the edit.
        for (final DiffNode node : codeNodes) {
            node.getFromLine().as(node.diffType);
            node.getToLine().as(node.diffType);
        }
        for (final DiffNode node : annotationNodes) {
            node.getFromLine().as(node.diffType);
            node.getToLine().as(node.diffType);
        }

        return new DiffTree(root, codeNodes, annotationNodes);
    }

    /**
     * Pops elements from the given stack until an if node is popped or the stack is empty.
     * @param stack The stack to pop the first if node from.
     * @return false if the stack is empty afterwards. Returns true otherwise (i.e., if an if code be popped).
     */
    private static boolean popIf(final Stack<DiffNode> stack, final DiffLineNumber currentLine) {
        // pop the relevant stacks until an if node is popped
        // If there were else or elif between an if and an endif, they are placed on the stack and
        // have to be popped now.
        DiffNode popped;
        boolean poppedElse = false;
        do {
            // dont update line numbers of popped nodes here as this already happened.
            popped = stack.pop();
            poppedElse |= popped.isElse() | popped.isElif();
        } while (!popped.isIf() && !popped.isRoot());

        // If the if had at least one else branch, its endline is already set.
        if (!poppedElse) {
            endBlock(popped, currentLine);
        }

        return !stack.isEmpty();
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
            endBlock(stack.peek(), lastLineNo);
        }

        stack.push(newNode);
        return ParseResult.SUCCESS;
    }

    /**
     * Adds a DiffNode as a child to its parents
     *
     * @param diffNode The DiffNode to be added as a child to its parents
     */
    static void addChildrenToParents(DiffNode diffNode) {
        if (diffNode.getAfterParent() != null) {
            diffNode.getAfterParent().addChild(diffNode);
        }
        if (diffNode.getBeforeParent() != null) {
            diffNode.getBeforeParent().addChild(diffNode);
        }
    }

    private static DiffNode endBlock(final DiffNode block, final DiffLineNumber lastLineNo) {
        // Add 1 because the end line is exclusive so we have to point one behind the last line we found
        block.getToLine().set(lastLineNo).add(1);
        return null;
    }
}
