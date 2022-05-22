package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.CodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;

import java.util.List;
import java.util.Stack;

import static org.variantsync.diffdetective.diff.result.DiffError.MLMACRO_WITHIN_MLMACRO;

public class MultiLineMacroParser {
    private final DiffNodeParser nodeParser;

    private MultilineMacro beforeMLMacro = null;
    private MultilineMacro afterMLMacro = null;

    public MultiLineMacroParser(DiffNodeParser nodeParser) {
        this.nodeParser = nodeParser;
    }

    /**
     * Converts a multiline macro to a DiffNode.
     * @param lineNo The end line number of the macro.
     * @param line The last line of the macro.
     * @param macro The macro to finalize.
     * @param diffType The diff type of the produced node.
     * @param nodes The list to add the node to.
     * @return The finalized macro converted to a DiffNode.
     */
    private DiffNode finalizeMLMacro(
            final DiffLineNumber lineNo,
            final String line,
            final MultilineMacro macro,
            final DiffType diffType,
            final List<DiffNode> nodes) throws IllFormedAnnotationException {
        macro.addLine(line);
        macro.diffType = diffType;

        final DiffNode node = macro.toDiffNode(nodeParser);
        node.getToLine().set(lineNo);
        nodes.add(node);
        return node;
    }

    ParseResult consume(
            final DiffLineNumber lineNo,
            final String line,
            final Stack<DiffNode> beforeStack,
            final Stack<DiffNode> afterStack,
            final List<DiffNode> nodes
    ) throws IllFormedAnnotationException {
        final DiffType diffType = DiffType.ofDiffLine(line);
        final boolean isAdd = diffType == DiffType.ADD;
        final boolean isRem = diffType == DiffType.REM;

        if (continuesMultilineDefinition(line)) {
            // If this multiline macro line is a header...
            final CodeType codeType = CodeType.ofDiffLine(line);
            if (codeType.isConditionalMacro()) {
                // ... create a new multi line macro to complete.
                if (!isAdd) {
                    if (beforeMLMacro != null) {
                        return ParseResult.ERROR(MLMACRO_WITHIN_MLMACRO, "Found definition of multiline macro within multiline macro at line " + line + "!");
                    }
                    beforeMLMacro = new MultilineMacro(line, lineNo, beforeStack.peek(), afterStack.peek());
                }
                if (!isRem) {
                    if (afterMLMacro != null) {
                        return ParseResult.ERROR(MLMACRO_WITHIN_MLMACRO, "Found definition of multiline macro within multiline macro at line " + line + "!");
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
                    beforeMLMacro.addLine(line);
                }
                if (!isRem) {
                    if (afterMLMacro == null) {
                        // see above
//                        return ParseResult.ERROR("Found line of a multiline macro without header at line " + line + "!");
                        return ParseResult.NOT_MY_DUTY;
                    }
                    afterMLMacro.addLine(line);
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
                        && diffType == DiffType.NON
                        && beforeMLMacro.equals(afterMLMacro)) {
                    // We have one single end line for to equal multi line macros -> Merge the nodes.
                    final DiffNode mlNode = finalizeMLMacro(
                            lineNo,
                            line,
                            beforeMLMacro /* == afterMLMacro */,
                            DiffType.NON, nodes);

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
                        final DiffNode beforeMLNode = finalizeMLMacro(
                                lineNo,
                                line,
                                beforeMLMacro,
                                DiffType.REM, nodes);

                        final ParseResult pushResult = DiffTreeParser.pushNodeToStack(beforeMLNode, beforeStack, beforeMLMacro.getLineFrom());
                        if (pushResult.isError()) {
                            return pushResult;
                        }

                        beforeMLMacro = null;
                    }

                    if (inAfterMLMacro && !isRem) {
                        final DiffNode afterMLNode = finalizeMLMacro(
                                lineNo,
                                line,
                                afterMLMacro, DiffType.ADD, nodes);

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
