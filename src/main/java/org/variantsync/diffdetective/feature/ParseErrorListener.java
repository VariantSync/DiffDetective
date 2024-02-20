package org.variantsync.diffdetective.feature;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.tinylog.Logger;
import org.variantsync.diffdetective.error.UncheckedUnParseableFormulaException;

import java.util.BitSet;

/**
 * A ParseErrorListener listens to syntactical errors discovered by an ANTLR parser while parsing a text. Encountered
 * errors are logged as warnings so that they can later be analyzed.
 * <p>
 * Logged warning might indicate that the ANTLR grammar used for parsing is imprecise or incomplete. However, it might
 * also simply be the case that the input text is indeed syntactically invalid.
 * </p>
 * @author Alexander Schultheiß
 */
public class ParseErrorListener implements ANTLRErrorListener {
    private final String formula;

    public ParseErrorListener(String formula) {
        this.formula = formula;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        Logger.warn("syntax error: {} ; {}", s, e);
        Logger.warn("formula: {}", formula);
        throw new UncheckedUnParseableFormulaException(s, e);
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
        // Do nothing
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
        // Do nothing
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
        // Do nothing
    }
}
