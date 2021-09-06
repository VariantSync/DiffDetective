package diff.difftree.parse;

import diff.difftree.DiffNode;

import java.util.List;
import java.util.Stack;

public class MultiLineMacroParser {
    private MultilineMacro beforeMLMacro = null;
    private MultilineMacro afterMLMacro = null;

    /**
     * Converts a multiline macro to a DiffNode.
     * @param macro The macro to finalize.
     * @param line The last line of the macro.
     * @param lineNo The end line number of the macro.
     * @param diffType The diff type of the produced node.
     * @param annotationNodes The list to add the node to.
     * @return The finalized macro converted to a DiffNode.
     */
    private static DiffNode finalizeMLMacro(
            final MultilineMacro macro,
            final String line,
            final int lineNo,
            final DiffNode.DiffType diffType,
            final List<DiffNode> annotationNodes) {
        macro.lines.add(line);
        macro.endLineInDiff = lineNo;
        macro.diffType = diffType;

        final DiffNode node = macro.toDiffNode();
        annotationNodes.add(node);
        DiffTreeParser.addChildrenToParents(node);
        return node;
    }

    ParseResult consume(
            final int lineNo,
            final String line,
            final Stack<DiffNode> beforeStack,
            final Stack<DiffNode> afterStack,
            final List<DiffNode> annotationNodes
    ) {
        final DiffNode.CodeType codeType = DiffNode.getCodeType(line);
        final DiffNode.DiffType diffType = DiffNode.getDiffType(line);
        final boolean isAdd = diffType == DiffNode.DiffType.ADD;
        final boolean isRem = diffType == DiffNode.DiffType.REM;

        if (continuesMultilineDefinition(line)) {
            // If this multiline macro line is a header...
            if (codeType.isConditionalMacro()) {
                // ... create a new multi line macro to complete.
                if (!isAdd) {
                    if (beforeMLMacro != null) {
                        return ParseResult.ERROR("Found definition of multiline macro within multiline macro at line " + line + "!");
                    }
                    beforeMLMacro = new MultilineMacro(line, lineNo, beforeStack.peek(), afterStack.peek());
                }
                if (!isRem) {
                    if (afterMLMacro != null) {
                        return ParseResult.ERROR("Found definition of multiline macro within multiline macro at line " + line + "!");
                    }
                    afterMLMacro = new MultilineMacro(line, lineNo, beforeStack.peek(), afterStack.peek());
                }
            } else { // body
                // ... otherwise, it is a line within a body of a multiline macro. Thus append it.
                if (!isAdd) {
                    if (beforeMLMacro == null) {
                        /* If this happens (at least) one of this happened
                         * 1. Found line of a multiline macro without header at line " + line + "!
                         * 2. Backslash in a comment.
                         * 3. It is the head of a multiline #define macro that we classify as code.
                         *
                         * As 2 and 3 are most likely we just assume those.
                         */
//                        return ParseResult.ERROR("Found line of a multiline macro without header at line " + line + "!");
                        return ParseResult.NOT_MY_DUTY;
                    }
                    beforeMLMacro.lines.add(line);
                }
                if (!isRem) {
                    if (afterMLMacro == null) {
                        // see above
//                        return ParseResult.ERROR("Found line of a multiline macro without header at line " + line + "!");
                        return ParseResult.NOT_MY_DUTY;
                    }
                    afterMLMacro.lines.add(line);
                }
            }

            return ParseResult.SUCCESS;
        } else {
            final boolean inBeforeMLMacro = beforeMLMacro != null;
            final boolean inAfterMLMacro = afterMLMacro != null;

            // check if last line of a multi macro
            if (inBeforeMLMacro || inAfterMLMacro) {
                if (
                        inBeforeMLMacro
                        && inAfterMLMacro
                        && diffType == DiffNode.DiffType.NON
                        && beforeMLMacro.equals(afterMLMacro)) {
                    // We have one single end line for to equal multi line macros -> Merge the nodes.
                    final DiffNode mlNode = finalizeMLMacro(beforeMLMacro /* == afterMLMacro */, line, lineNo, DiffNode.DiffType.NON, annotationNodes);

                    ParseResult pushResult = DiffTreeParser.pushNodeToStack(mlNode, beforeStack, beforeMLMacro.getLineFrom());
                    if (pushResult.isError()) {
                        return pushResult;
                    }

                    pushResult = DiffTreeParser.pushNodeToStack(mlNode, afterStack, afterMLMacro.getLineFrom());
                    if (pushResult.isError()) {
                        return pushResult;
                    }

                    beforeMLMacro = null;
                    afterMLMacro = null;
                } else {
                    if (inBeforeMLMacro && !isAdd) {
                        final DiffNode beforeMLNode = finalizeMLMacro(beforeMLMacro, line, lineNo, DiffNode.DiffType.REM, annotationNodes);

                        final ParseResult pushResult = DiffTreeParser.pushNodeToStack(beforeMLNode, beforeStack, beforeMLMacro.getLineFrom());
                        if (pushResult.isError()) {
                            return pushResult;
                        }

                        beforeMLMacro = null;
                    }

                    if (inAfterMLMacro && !isRem) {
                        final DiffNode afterMLNode = finalizeMLMacro(afterMLMacro, line, lineNo, DiffNode.DiffType.ADD, annotationNodes);

                        final ParseResult pushResult = DiffTreeParser.pushNodeToStack(afterMLNode, afterStack, afterMLMacro.getLineFrom());
                        if (pushResult.isError()) {
                            return pushResult;
                        }

                        afterMLMacro = null;
                    }
                }

                return ParseResult.SUCCESS;
            }
        }

        return ParseResult.NOT_MY_DUTY;
    }

    public static boolean continuesMultilineDefinition(String line) {
        return line.trim().endsWith("\\");
    }
}
