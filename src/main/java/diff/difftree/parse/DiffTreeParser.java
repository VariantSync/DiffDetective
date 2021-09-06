package diff.difftree.parse;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import org.pmw.tinylog.Logger;

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

        DiffNode lastCode = null;
        Consumer<String> errorHandler = Logger::warn;

        final MultiLineMacroParser mlMacroParser = new MultiLineMacroParser();

        final DiffNode root = DiffNode.createRoot();
        beforeStack.push(root);
        afterStack.push(root);

        for (int i = 0; i < fullDiffLines.length; i++) {
            final String currentLine = fullDiffLines[i];

            // Ignore line if it is empty.
            if (ignoreEmptyLines && (currentLine.length() == 0
                    // substring(1) here because of diff symbol ('+', '-', ' ') at the beginning of a line.
                    || currentLine.substring(1).isEmpty())) {
                // discard empty lines
                continue;
            }

            // check if this is a multiline macro
            final ParseResult isMLMacro = mlMacroParser.consume(i, currentLine, beforeStack, afterStack, annotationNodes);
            switch (isMLMacro.type()) {
                case Success: {
                    // This line belongs to a multiline macro and was handled, so go to the next line.
                    lastCode = null;
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
            final DiffNode newNode = DiffNode.fromLine(currentLine, beforeStack.peek(), afterStack.peek());

            // collapse multiple code lines
            if (collapseMultipleCodeLines && lastCode != null && newNode.isCode()
                    && lastCode.diffType.equals(newNode.diffType)) {
                continue;
            } else if (lastCode != null) {
                lastCode.setToLine(i);
            }

            if (newNode.isCode()) {
                lastCode = newNode;
            } else {
                lastCode = null;
            }

            if (newNode.isCode()) {
                newNode.setFromLine(i);
                codeNodes.add(newNode);
                addChildrenToParents(newNode);
            } else if (newNode.isEndif()) {
                if (!newNode.isAdd()) {
                    // set corresponding line of now closed annotation
                    beforeStack.peek().setToLine(i);

                    // pop the relevant stacks until an if node is popped
                    if (!popIf(beforeStack)) {
                        errorHandler.accept("(before-) stack is empty!");
                        return null;
                    }
                }
                if (!newNode.isRem()) {
                    // set corresponding line of now closed annotation
                    afterStack.peek().setToLine(i);

                    // pop the relevant stacks until an if node is popped
                    if (!popIf(afterStack)) {
                        errorHandler.accept("(after-) stack is empty!");
                        return null;
                    }
                }

            } else {
                // newNode is if, elif or else
                // push the node to the relevant stacks
                if (!newNode.isAdd()) {
                    if (pushNodeToStack(newNode, beforeStack, i).onError(errorHandler)) {
                        return null;
                    }
                }
                if (!newNode.isRem()) {
                    if (pushNodeToStack(newNode, afterStack, i).onError(errorHandler)) {
                        return null;
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
            errorHandler.accept("Not all annotations closed!");
            return null;
        }

        return new DiffTree(root, codeNodes, annotationNodes);
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

    static ParseResult pushNodeToStack(final DiffNode newNode, final Stack<DiffNode> stack, int currentLine) {
        if (newNode.isElif() || newNode.isElse()) {
            if (stack.size() == 1) {
                return ParseResult.ERROR("#else or #elif without if!");
            }

            // set corresponding line of now closed annotation
            stack.peek().setToLine(currentLine - 1);
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
}
