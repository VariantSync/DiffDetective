package org.variantsync.diffdetective.variation.diff.parse;

import org.apache.commons.lang3.function.FailableSupplier;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Parser that parses {@link DiffTree}s from text-based diffs.
 * <p>
 * Note: Weird line continuations and comments can cause misidentification of conditional macros.
 * The following examples are all correct according to the C11 standard: (comment end is marked by
 * {@code *\/}):
 * <code>
 * /*
 * #ifdef A
 * *\/
 *
 * #ifdef /*
 * *\/ A
 * #endif
 *
 * # /**\/ ifdef
 * #endif
 *
 * # \
 *   ifdef
 * #endif
 * </code>
 */
public class DiffTreeParser {
    /**
     * One line of a diff.
     * In contrast to {@link LogicalLine}, this represents a physical line corresponding to a diff
     * instead of some source code file.
     *
     * @param diffType the diff type of this line, may be {@code null} if this line has no valid
     * diff type
     * @param content the actual line content without a line delimiter
     */
    public record DiffLine(DiffType diffType, String content) {}

    /**
     * Matches the beginning of conditional macros.
     * It doesn't match the whole macro name, for example for {@code #ifdef} only {@code "#if"} is
     * matched and only {@code "if"} is captured.
     * <p>
     * Note that this pattern doesn't handle comments between {@code #} and the macro name.
     */
    private final static Pattern macroPattern =
        Pattern.compile("^[+-]?\\s*#\\s*(if|elif|else|endif)");


    /* Settings */
    final DiffTreeParseOptions options;


    /* State */

    /**
     * A stack containing the current path before the edit from the root of the currently parsed
     * {@link DiffTree} to the currently parsed {@link DiffNode}.
     * <p>
     * The granularity of the {@link DiffTree}s parsed by this class is always lines because line
     * diffs can't represent different granularities. This implies that there are no nested artifact
     * nodes in the resulting {@link DiffTree} and therefore {@code beforeStack} will never contain
     * an artifact node.
     */
    private final Stack<DiffNode> beforeStack = new Stack<>();
    /**
     * A stack containing the current path after the edit from the root of the currently parsed
     * {@link DiffTree} to the currently parsed {@link DiffNode}.
     * <p>
     * See {@link #beforeStack} for more explanations.
     */
    private final Stack<DiffNode> afterStack = new Stack<>();

    /**
     * The last artifact node which was parsed by {@link #parseLine}.
     * If the last parsed {@code DiffNode} was not an artifact, {@code lastArtifact} is {@code null}.
     * <p>
     * This state is used to implement {@link DiffTreeParseOptions#collapseMultipleCodeLines()}.
     */
    private DiffNode lastArtifact = null;


    /**
     * The same as {@link DiffTreeParser#createDiffTree(BufferedReader, DiffTreeParseOptions)}
     * but with the diff given as a single string with line breaks instead of a {@link BufferedReader}.
     *
     * @throws DiffParseException if {@code fullDiff} couldn't be parsed
     */
    public static DiffTree createDiffTree(
            final String fullDiff,
            final DiffTreeParseOptions parseOptions
    ) throws DiffParseException {
        try {
            return createDiffTree(new BufferedReader(new StringReader(fullDiff)), parseOptions);
        } catch (IOException e) {
            throw new AssertionError("No actual IO should be performed because only a StringReader is used");
        }
    }

    /**
     * Default parsing method for {@link DiffTree}s from diffs.
     * This implementation has options to collapse multiple code lines into one node and to
     * discard empty lines.
     * This parsing algorithm is described in detail in Sören Viegener's bachelor's thesis.
     *
     * @param fullDiff The full diff of a patch obtained from a buffered reader.
     * @param options {@link DiffTreeParseOptions} for the parsing process.
     * @return A parsed {@link DiffTree} upon success or an error indicating why parsing failed.
     * @throws IOException when reading from {@code fullDiff} fails.
     * @throws DiffParseException if an error in the diff or macro syntax is detected
     */
    public static DiffTree createDiffTree(
            BufferedReader fullDiff,
            final DiffTreeParseOptions options
    ) throws IOException, DiffParseException {
        return new DiffTreeParser(
            options
        ).parse(() -> {
            String line = fullDiff.readLine();
            if (line == null) {
                return null;
            } else {
                return new DiffLine(
                        DiffType.ofDiffLine(line),
                        line.isEmpty() ? line : line.substring(1)
                );
            }
        });
    }

    /**
     * Parses a variation tree from a source file.
     * This method is similar to {@link #createDiffTree(BufferedReader, DiffTreeParseOptions)}
     * but acts as if all lines where unmodified.
     *
     * @param file The source code file (not a diff) to be parsed.
     * @param options {@link DiffTreeParseOptions} for the parsing process.
     * @return A parsed {@link DiffTree}.
     * @throws IOException iff {@code file} throws an {@code IOException}
     * @throws DiffParseException if an error in the diff or macro syntax is detected
     */
    public static DiffTree createVariationTree(
            BufferedReader file,
            DiffTreeParseOptions options
    ) throws IOException, DiffParseException {
        return new DiffTreeParser(
            options
        ).parse(() -> {
            String line = file.readLine();
            if (line == null) {
                return null;
            } else {
                if (line.startsWith("+") || line.startsWith("-")) {
                    Logger.warn(
                        "The source file given to createVariationTree contains a plus or " +
                        "minus sign at the start of a line. Please ensure that you are " +
                        "actually parsing a source file and not a diff."
                    );
                }

                return new DiffLine(DiffType.NON, line);
            }
        });
    }

    /**
     * Initializes the parse state.
     *
     * @see #createDiffTree(BufferedReader, DiffTreeParseOptions)
     */
    private DiffTreeParser(
            DiffTreeParseOptions options
    ) {
        this.options = options;
    }

    /**
     * Parses the line diff {@code fullDiff}.
     *
     * @param lines should supply successive lines of the diff to be parsed, or {@code null} if
     * there are no more lines to be parsed.
     * @return the parsed {@code DiffTree}
     * @throws IOException iff {@code lines.get()} throws {@code IOException}
     * @throws DiffParseException if an error in the line diff or the underlying preprocessor syntax
     * is detected
     */
    private DiffTree parse(
        FailableSupplier<DiffLine, IOException> lines
    ) throws IOException, DiffParseException {
        DiffNode root = DiffNode.createRoot();
        beforeStack.push(root);
        afterStack.push(root);

        final LogicalLine beforeLine = new LogicalLine();
        final LogicalLine afterLine = new LogicalLine();
        boolean isNon = false;

        DiffLineNumber lineNumber = new DiffLineNumber(0, 0, 0);
        DiffLine currentDiffLine;
        while ((currentDiffLine = lines.get()) != null) {
            final String currentLine = currentDiffLine.content();

            final DiffType diffType = currentDiffLine.diffType();
            if (diffType == null) {
                throw new DiffParseException(DiffError.INVALID_DIFF, lineNumber.add(1));
            }

            lineNumber = lineNumber.add(1, diffType);

            // Ignore line if it is empty.
            if (options.ignoreEmptyLines() && currentLine.isBlank()) {
                // discard empty lines
                continue;
            }

            // Do beforeLine and afterLine represent the same unchanged diff line?
            isNon = diffType == DiffType.NON &&
                (isNon || (!beforeLine.hasStarted() && !afterLine.hasStarted()));

            // Add the physical line to the logical line.
            final DiffLineNumber lineNumberFinal = lineNumber;
            diffType.forAllTimesOfExistence(beforeLine, afterLine,
                node -> node.consume(currentLine, lineNumberFinal)
            );

            // Parse the completed logical line
            if (isNon && beforeLine.isComplete() && afterLine.isComplete()) {
                // Only parse it once if beforeLine and afterLine represent the same unchanged
                // diff line.
                parseLine(beforeLine, DiffType.NON, lineNumber);
                beforeLine.reset();
                afterLine.reset();
            } else {
                if (beforeLine.isComplete()) {
                    parseLine(beforeLine, DiffType.REM, lineNumber);
                    beforeLine.reset();
                }
                if (afterLine.isComplete()) {
                    parseLine(afterLine, DiffType.ADD, lineNumber);
                    afterLine.reset();
                }
            }
        }

        if (beforeLine.hasStarted() || afterLine.hasStarted()) {
            throw new DiffParseException(
                DiffError.INVALID_LINE_CONTINUATION,
                lineNumber
            );
        }

        if (beforeStack.size() > 1) {
            throw new DiffParseException(
                DiffError.NOT_ALL_ANNOTATIONS_CLOSED,
                beforeStack.peek().getFromLine()
            );
        }
        if (afterStack.size() > 1) {
            throw new DiffParseException(
                DiffError.NOT_ALL_ANNOTATIONS_CLOSED,
                afterStack.peek().getFromLine()
            );
        }

        // Cleanup state
        beforeStack.clear();
        afterStack.clear();
        lastArtifact = null;

        return new DiffTree(root);
    }

    /**
     * Parses one logical line and most notably, handles conditional macros.
     *
     * @param line a logical line with {@code line.isComplete() == true}
     * @param diffType whether {@code line} was added, inserted or unchanged
     * @param lastLineNumber the last physical line of {@code line}
     * @throws DiffParseException if erroneous preprocessor macros are detected
     */
    private void parseLine(
            final LogicalLine line,
            final DiffType diffType,
            final DiffLineNumber lastLineNumber
    ) throws DiffParseException {
        final DiffLineNumber fromLine = line.getStartLineNumber().as(diffType);
        final DiffLineNumber toLine = lastLineNumber.add(1).as(diffType);

        // Is this line a conditional macro?
        // Note: The following line doesn't handle comments and line continuations correctly.
        var matcher = macroPattern.matcher(line.getLines().get(0));
        var conditionalMacroName = matcher.find()
            ? matcher.group(1)
            : null;

        if ("endif".equals(conditionalMacroName)) {
            lastArtifact = null;

            // Do not create a node for ENDIF, but update the line numbers of the closed if-chain
            // and remove that if-chain from the relevant stacks.
            diffType.forAllTimesOfExistence(beforeStack, afterStack, stack ->
                popIfChain(stack, fromLine)
            );
        } else if (options.collapseMultipleCodeLines()
                && conditionalMacroName == null
                && lastArtifact != null
                && lastArtifact.diffType.equals(diffType)
                && lastArtifact.getToLine().inDiff() == fromLine.inDiff()) {
            // Collapse consecutive lines if possible.
            lastArtifact.addLines(line.getLines());
            lastArtifact.setToLine(toLine);
        } else {
            try {
                NodeType nodeType = NodeType.ARTIFACT;
                if (conditionalMacroName != null) {
                    try {
                        nodeType = NodeType.fromName(conditionalMacroName);
                    } catch (IllegalArgumentException e) {
                        throw new DiffParseException(DiffError.INVALID_MACRO_NAME, fromLine);
                    }
                }

                DiffNode newNode = new DiffNode(
                    diffType,
                    nodeType,
                    fromLine,
                    toLine,
                    nodeType == NodeType.ARTIFACT || nodeType == NodeType.ELSE
                        ? null
                        : options.annotationParser().parseDiffLines(line.getLines()),
                    line.getLines()
                );

                addNode(newNode);
                lastArtifact = newNode.isArtifact() ? newNode : null;
            } catch (IllFormedAnnotationException e) {
                throw new DiffParseException(e.getType(), fromLine);
            }
        }
    }

    /**
     * Pop {@code stack} until an IF node is popped.
     * If there were ELSEs or ELIFs between an IF and an ENDIF, they were placed on the stack and
     * have to be popped now. The {@link DiffNode#getToLine() end line numbers} are adjusted
     *
     * @param stack the stack which should be popped
     * @param elseLineNumber the first line of the else which causes this IF to be popped
     * @throws DiffParseException if {@code stack} doesn't contain an IF node
     */
    private void popIfChain(
        Stack<DiffNode> stack,
        DiffLineNumber elseLineNumber
    ) throws DiffParseException {
        DiffLineNumber previousLineNumber = elseLineNumber;
        do {
            DiffNode annotation = stack.peek();

            // Set the line number of now closed annotations to the beginning of the
            // following annotation.
            annotation.setToLine(new DiffLineNumber(
                Math.max(previousLineNumber.inDiff(), annotation.getToLine().inDiff()),
                stack == beforeStack
                    ? previousLineNumber.beforeEdit()
                    : annotation.getToLine().beforeEdit(),
                stack == afterStack
                    ? previousLineNumber.afterEdit()
                    : annotation.getToLine().afterEdit()
            ));

            previousLineNumber = annotation.getFromLine();
        } while (!stack.pop().isIf());

        if (stack.isEmpty()) {
            throw new DiffParseException(DiffError.ENDIF_WITHOUT_IF, elseLineNumber);
        }
    }

    /**
     * Adds a fully parsed node into the {@code DiffTree}.
     * Annotations also pushed to the relevant stacks.
     *
     * @param newNode the fully parsed node to be added
     * @throws DiffParseException if {@code line} erroneous preprocessor macros are detected
     */
    private void addNode(DiffNode newNode) throws DiffParseException {
        newNode.addBelow(beforeStack.peek(), afterStack.peek());

        if (newNode.isAnnotation()) {
            // newNode is IF, ELIF or ELSE, so push it to the relevant stacks.
            newNode.diffType.forAllTimesOfExistence(beforeStack, afterStack, stack -> {
                if (newNode.isElif() || newNode.isElse()) {
                    if (stack.size() == 1) {
                        throw new DiffParseException(
                            DiffError.ELSE_OR_ELIF_WITHOUT_IF,
                            newNode.getFromLine()
                        );
                    }

                    if (stack.peek().isElse()) {
                        throw new DiffParseException(DiffError.ELSE_AFTER_ELSE, newNode.getFromLine());
                    }
                }

                stack.push(newNode);
            });
        }
    }

    /**
     * Parses the given commit of the given repository.
     * @param repo The repository from which a commit should be parsed.
     * @param commitHash Hash of the commit to parse.
     * @return A CommitDiff describing edits to variability introduced by the given commit relative
     * to its first parent commit.
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
     * @return A PatchDiff describing edits to variability introduced by the given patch relative to
     * the corresponding commit's first parent commit.
     * @throws IOException when an error occurred.
     * @throws AssertionError when no such patch exists.
     */
    public static PatchDiff parsePatch(Repository repo, String file, String commitHash) throws IOException {
        final CommitDiff commitDiff = parseCommit(repo, commitHash);

        for (final PatchDiff pd : commitDiff.getPatchDiffs()) {
            if (file.equals(pd.getFileName(Time.AFTER))) {
                return pd;
            }
        }

        Assert.fail("Did not find file \"" + file + "\" in commit " + commitHash + "!");
        return null;
    }
}
