package org.variantsync.diffdetective.diff.difftree.parse;

import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffParseException;

import java.io.BufferedReader;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * A parser for definitions of multiline macros in text-based diffs.
 * @author Paul Bittner
 */
public class MultiLineMacroParser {
    /** Matches conditional macros. Note that it doesn't match the whole line or even the whole
     * macro name. For example {@code #ifdef} is also matched, but only {@code "if"} is captured.
     */
    private final static Pattern macroPattern = Pattern.compile("^[+-]?\\s*#\\s*(if|elif|else|endif)");

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
     * @return The finalized macro converted to a DiffNode.
     */
    private DiffNode finalizeMLMacro(
            final DiffLineNumber lineNo,
            final String line,
            final MultilineMacro macro,
            final DiffType diffType) throws IllFormedAnnotationException {
        macro.addLine(line);
        macro.diffType = diffType;

        final DiffNode node = macro.toDiffNode(nodeParser);
        node.setToLine(lineNo);
        return node;
    }

    /**
     * Consumes the next line a text-based diff and determines if that line
     * is part of a multi-line macro definition or not.
     * @param lineNo The line number of the currently parsed line.
     * @param line The line to parse.
     * @param beforeStack The current before stack as defined by Sören's algorithm.
     * @param afterStack The current after stack as defined by Sören's algorithm.
     * @return true iff the line was consumed and is part of a multiline macro definition.
     * @throws IllFormedAnnotationException when {@link MultiLineMacroParser#finalizeMLMacro} fails.
     * @see DiffTreeParser#createDiffTree(BufferedReader, boolean, boolean, DiffNodeParser)
     */
    boolean consume(
            final DiffLineNumber lineNo,
            final String line,
            final Stack<DiffNode> beforeStack,
            final Stack<DiffNode> afterStack
    ) throws IllFormedAnnotationException, DiffParseException {
        final DiffType diffType = DiffType.ofDiffLine(line);
        final boolean isAdd = diffType == DiffType.ADD;
        final boolean isRem = diffType == DiffType.REM;

        if (continuesMultilineDefinition(line)) {
            // If this multiline macro line is a header...
            if (conditionalMacroName(line) != null) {
                // ... create a new multi line macro to complete.
                if (!isAdd) {
                    if (beforeMLMacro != null) {
                        throw new DiffParseException(DiffError.MLMACRO_WITHIN_MLMACRO, lineNo);
                    }
                    beforeMLMacro = new MultilineMacro(line, diffType, lineNo, beforeStack.peek(), afterStack.peek());
                }
                if (!isRem) {
                    if (afterMLMacro != null) {
                        throw new DiffParseException(DiffError.MLMACRO_WITHIN_MLMACRO, lineNo);
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
                         * 3. It is the head of a multiline #define macro that we classify as artifact.
                         *
                         * As 2 and 3 are most likely we just assume those.
                         */
                        return false;
                    }
                    beforeMLMacro.addLine(line);
                }
                if (!isRem) {
                    if (afterMLMacro == null) {
                        // see above
                        return false;
                    }
                    afterMLMacro.addLine(line);
                }
            }

            return true;
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
                            DiffType.NON);

                    DiffTreeParser.pushNodeToStack(mlNode, beforeStack, beforeMLMacro.getLineFrom());
                    DiffTreeParser.pushNodeToStack(mlNode, afterStack, afterMLMacro.getLineFrom());

                    beforeMLMacro = null;
                    afterMLMacro = null;
                } else {
                    if (inBeforeMLMacro && !isAdd) {
                        final DiffNode beforeMLNode = finalizeMLMacro(
                                lineNo,
                                line,
                                beforeMLMacro,
                                DiffType.REM);

                        DiffTreeParser.pushNodeToStack(beforeMLNode, beforeStack, beforeMLMacro.getLineFrom());

                        beforeMLMacro = null;
                    }

                    if (inAfterMLMacro && !isRem) {
                        final DiffNode afterMLNode = finalizeMLMacro(
                                lineNo,
                                line,
                                afterMLMacro, DiffType.ADD);

                        DiffTreeParser.pushNodeToStack(afterMLNode, afterStack, afterMLMacro.getLineFrom());

                        afterMLMacro = null;
                    }
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the given line continues the definition of a multiline macro (possibly within a text-based diff).
     * @param line The line to check for continuing a multiline macro definition.
     * @return True iff the line ends with a backslash.
     */
    public static boolean continuesMultilineDefinition(String line) {
        return line.trim().endsWith("\\");
    }

    /**
     * Returns the shortened name of a conditional macro.
     *
     * Shortened means it's one of {@code if}, {@code elif}, {@code else} or {@code endif},
     * although the actual macro name may be longer (for example {@code ifdef}).
     *
     * @param line the first line of the macro
     * @return the shortened name of a conditional macro in {@code line} or {@code null} if there
     * is no conditional macro on {@code line}
     */
    public static String conditionalMacroName(String line) {
        var matcher = macroPattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
