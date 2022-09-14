package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.NodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;

import java.io.BufferedReader;
import java.util.List;
import java.util.Stack;

import static org.variantsync.diffdetective.diff.result.DiffError.MLMACRO_WITHIN_MLMACRO;

/**
 * A parser for definitions of multiline macros in text-based diffs.
 * @author Paul Bittner
 */
public class MultiLineMacroParser {
    private final DiffNodeParser nodeParser;

    private MultilineMacro beforeMLMacro = null;
    private MultilineMacro afterMLMacro = null;

    /**
     * Create a new parser that uses the given DiffNodeParser to construct DiffNodes.
     * @param nodeParser Parser to build DiffNodes.
     */
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

    /**
     * Consumes the next line a text-based diff and determines if that line
     * is part of a multi-line macro definition or not.
     * @param lineNo The line number of the currently parsed line.
     * @param line The line to parse.
     * @param beforeStack The current before stack as defined by Sören's algorithm.
     * @param afterStack The current after stack as defined by Sören's algorithm.
     * @param nodes The list of all DiffNodes that where already parsed.
     * @return {@link ParseResult#SUCCESS} if the line was consumed and is part of a multiline macro definition.
     *         {@link ParseResult#NOT_MY_DUTY} if the line is not part of a multiline macro definition and was not parsed.
     *         The line remains unparsed and should be parsed in another way.
     *         {@link ParseResult#ERROR} if an error occurred (e.g., because of a syntax error).
     * @throws IllFormedAnnotationException when {@link MultiLineMacroParser#finalizeMLMacro} fails.
     * @see DiffTreeParser#createDiffTree(BufferedReader, boolean, boolean, DiffNodeParser)
     */
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
            final NodeType nodeType = NodeType.ofDiffLine(line);
            if (nodeType.isConditionalAnnotation()) {
                // ... create a new multi line macro to complete.
                if (!isAdd) {
                    if (beforeMLMacro != null) {
                        return ParseResult.ERROR(MLMACRO_WITHIN_MLMACRO, "Found definition of multiline macro within multiline macro at line " + line + "!");
                    }
                    beforeMLMacro = new MultilineMacro(line, diffType, lineNo, beforeStack.peek(), afterStack.peek());
                }
                if (!isRem) {
                    if (afterMLMacro != null) {
                        return ParseResult.ERROR(MLMACRO_WITHIN_MLMACRO, "Found definition of multiline macro within multiline macro at line " + line + "!");
                    }
                    afterMLMacro = new MultilineMacro(line, diffType, lineNo, beforeStack.peek(), afterStack.peek());
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
            // We either found the ending line of a multiline macro or just a plain line outside of a macro.
            final boolean inBeforeMLMacro = beforeMLMacro != null;
            final boolean inAfterMLMacro = afterMLMacro != null;

            // check if last line of a multi macro
            if (inBeforeMLMacro || inAfterMLMacro) {
                if (
                        inBeforeMLMacro
                        && inAfterMLMacro
                        && diffType == DiffType.NON
                        && beforeMLMacro.equals(afterMLMacro)) {
                    // We have one single end line for two equal multi line macros -> Merge the nodes.
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

    /**
     * Checks whether the given line continues the definition of a multiline macro (possibly within a text-based diff).
     * @param line The line to check for continuing a multiline macro definition.
     * @return True iff the line ends with a backslash.
     */
    public static boolean continuesMultilineDefinition(String line) {
        return line.trim().endsWith("\\");
    }
}
