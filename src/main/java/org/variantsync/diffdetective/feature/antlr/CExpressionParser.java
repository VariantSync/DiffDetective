// Generated from /home/alex/programming/DiffDetective/src/main/resources/grammars/CExpression.g4 by ANTLR 4.13.1
package org.variantsync.diffdetective.feature.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class CExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LeftParen=1, RightParen=2, LeftBracket=3, RightBracket=4, LeftBrace=5, 
		RightBrace=6, Less=7, LessEqual=8, Greater=9, GreaterEqual=10, LeftShift=11, 
		RightShift=12, Plus=13, PlusPlus=14, Minus=15, MinusMinus=16, Star=17, 
		Div=18, Mod=19, And=20, Or=21, AndAnd=22, OrOr=23, Caret=24, Not=25, Tilde=26, 
		Question=27, Colon=28, Semi=29, Comma=30, Assign=31, StarAssign=32, DivAssign=33, 
		ModAssign=34, PlusAssign=35, MinusAssign=36, LeftShiftAssign=37, RightShiftAssign=38, 
		AndAssign=39, XorAssign=40, OrAssign=41, Equal=42, NotEqual=43, Arrow=44, 
		Dot=45, Ellipsis=46, HasAttribute=47, HasCPPAttribute=48, HasCAttribute=49, 
		HasBuiltin=50, HasInclude=51, Identifier=52, Constant=53, DigitSequence=54, 
		StringLiteral=55, PathLiteral=56, MultiLineMacro=57, Directive=58, AsmBlock=59, 
		Whitespace=60, Newline=61, BlockComment=62, LineComment=63;
	public static final int
		RULE_conditionalExpression = 0, RULE_primaryExpression = 1, RULE_unaryOperator = 2, 
		RULE_multiplicativeExpression = 3, RULE_additiveExpression = 4, RULE_shiftExpression = 5, 
		RULE_relationalExpression = 6, RULE_equalityExpression = 7, RULE_andExpression = 8, 
		RULE_exclusiveOrExpression = 9, RULE_inclusiveOrExpression = 10, RULE_specialOperator = 11, 
		RULE_logicalAndExpression = 12, RULE_logicalOrExpression = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"conditionalExpression", "primaryExpression", "unaryOperator", "multiplicativeExpression", 
			"additiveExpression", "shiftExpression", "relationalExpression", "equalityExpression", 
			"andExpression", "exclusiveOrExpression", "inclusiveOrExpression", "specialOperator", 
			"logicalAndExpression", "logicalOrExpression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'['", "']'", "'{'", "'}'", "'<'", "'<='", "'>'", 
			"'>='", "'<<'", "'>>'", "'+'", "'++'", "'-'", "'--'", "'*'", "'/'", "'%'", 
			"'&'", "'|'", "'&&'", "'||'", "'^'", "'!'", "'~'", "'?'", "':'", "';'", 
			"','", "'='", "'*='", "'/='", "'%='", "'+='", "'-='", "'<<='", "'>>='", 
			"'&='", "'^='", "'|='", "'=='", "'!='", "'->'", "'.'", "'...'", "'__has_attribute'", 
			"'__has_cpp_attribute'", "'__has_c_attribute'", "'__has_builtin'", "'__has_include'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LeftParen", "RightParen", "LeftBracket", "RightBracket", "LeftBrace", 
			"RightBrace", "Less", "LessEqual", "Greater", "GreaterEqual", "LeftShift", 
			"RightShift", "Plus", "PlusPlus", "Minus", "MinusMinus", "Star", "Div", 
			"Mod", "And", "Or", "AndAnd", "OrOr", "Caret", "Not", "Tilde", "Question", 
			"Colon", "Semi", "Comma", "Assign", "StarAssign", "DivAssign", "ModAssign", 
			"PlusAssign", "MinusAssign", "LeftShiftAssign", "RightShiftAssign", "AndAssign", 
			"XorAssign", "OrAssign", "Equal", "NotEqual", "Arrow", "Dot", "Ellipsis", 
			"HasAttribute", "HasCPPAttribute", "HasCAttribute", "HasBuiltin", "HasInclude", 
			"Identifier", "Constant", "DigitSequence", "StringLiteral", "PathLiteral", 
			"MultiLineMacro", "Directive", "AsmBlock", "Whitespace", "Newline", "BlockComment", 
			"LineComment"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConditionalExpressionContext extends ParserRuleContext {
		public LogicalOrExpressionContext logicalOrExpression() {
			return getRuleContext(LogicalOrExpressionContext.class,0);
		}
		public TerminalNode Question() { return getToken(CExpressionParser.Question, 0); }
		public List<ConditionalExpressionContext> conditionalExpression() {
			return getRuleContexts(ConditionalExpressionContext.class);
		}
		public ConditionalExpressionContext conditionalExpression(int i) {
			return getRuleContext(ConditionalExpressionContext.class,i);
		}
		public TerminalNode Colon() { return getToken(CExpressionParser.Colon, 0); }
		public ConditionalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterConditionalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitConditionalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitConditionalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionalExpressionContext conditionalExpression() throws RecognitionException {
		ConditionalExpressionContext _localctx = new ConditionalExpressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_conditionalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			logicalOrExpression();
			setState(34);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Question) {
				{
				setState(29);
				match(Question);
				setState(30);
				conditionalExpression();
				setState(31);
				match(Colon);
				setState(32);
				conditionalExpression();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExpressionContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CExpressionParser.Identifier, 0); }
		public TerminalNode Constant() { return getToken(CExpressionParser.Constant, 0); }
		public List<TerminalNode> StringLiteral() { return getTokens(CExpressionParser.StringLiteral); }
		public TerminalNode StringLiteral(int i) {
			return getToken(CExpressionParser.StringLiteral, i);
		}
		public TerminalNode LeftParen() { return getToken(CExpressionParser.LeftParen, 0); }
		public ConditionalExpressionContext conditionalExpression() {
			return getRuleContext(ConditionalExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CExpressionParser.RightParen, 0); }
		public UnaryOperatorContext unaryOperator() {
			return getRuleContext(UnaryOperatorContext.class,0);
		}
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterPrimaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitPrimaryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitPrimaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_primaryExpression);
		int _la;
		try {
			setState(50);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(36);
				match(Identifier);
				}
				break;
			case Constant:
				enterOuterAlt(_localctx, 2);
				{
				setState(37);
				match(Constant);
				}
				break;
			case StringLiteral:
				enterOuterAlt(_localctx, 3);
				{
				setState(39); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(38);
					match(StringLiteral);
					}
					}
					setState(41); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==StringLiteral );
				}
				break;
			case LeftParen:
				enterOuterAlt(_localctx, 4);
				{
				setState(43);
				match(LeftParen);
				setState(44);
				conditionalExpression();
				setState(45);
				match(RightParen);
				}
				break;
			case Plus:
			case Minus:
			case Star:
			case And:
			case Not:
			case Tilde:
				enterOuterAlt(_localctx, 5);
				{
				setState(47);
				unaryOperator();
				setState(48);
				primaryExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnaryOperatorContext extends ParserRuleContext {
		public TerminalNode And() { return getToken(CExpressionParser.And, 0); }
		public TerminalNode Star() { return getToken(CExpressionParser.Star, 0); }
		public TerminalNode Plus() { return getToken(CExpressionParser.Plus, 0); }
		public TerminalNode Minus() { return getToken(CExpressionParser.Minus, 0); }
		public TerminalNode Tilde() { return getToken(CExpressionParser.Tilde, 0); }
		public TerminalNode Not() { return getToken(CExpressionParser.Not, 0); }
		public UnaryOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterUnaryOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitUnaryOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitUnaryOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryOperatorContext unaryOperator() throws RecognitionException {
		UnaryOperatorContext _localctx = new UnaryOperatorContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_unaryOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 101883904L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeExpressionContext extends ParserRuleContext {
		public List<PrimaryExpressionContext> primaryExpression() {
			return getRuleContexts(PrimaryExpressionContext.class);
		}
		public PrimaryExpressionContext primaryExpression(int i) {
			return getRuleContext(PrimaryExpressionContext.class,i);
		}
		public List<TerminalNode> Star() { return getTokens(CExpressionParser.Star); }
		public TerminalNode Star(int i) {
			return getToken(CExpressionParser.Star, i);
		}
		public List<TerminalNode> Div() { return getTokens(CExpressionParser.Div); }
		public TerminalNode Div(int i) {
			return getToken(CExpressionParser.Div, i);
		}
		public List<TerminalNode> Mod() { return getTokens(CExpressionParser.Mod); }
		public TerminalNode Mod(int i) {
			return getToken(CExpressionParser.Mod, i);
		}
		public MultiplicativeExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicativeExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterMultiplicativeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitMultiplicativeExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitMultiplicativeExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicativeExpressionContext multiplicativeExpression() throws RecognitionException {
		MultiplicativeExpressionContext _localctx = new MultiplicativeExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_multiplicativeExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			primaryExpression();
			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 917504L) != 0)) {
				{
				{
				setState(55);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 917504L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(56);
				primaryExpression();
				}
				}
				setState(61);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveExpressionContext extends ParserRuleContext {
		public List<MultiplicativeExpressionContext> multiplicativeExpression() {
			return getRuleContexts(MultiplicativeExpressionContext.class);
		}
		public MultiplicativeExpressionContext multiplicativeExpression(int i) {
			return getRuleContext(MultiplicativeExpressionContext.class,i);
		}
		public List<TerminalNode> Plus() { return getTokens(CExpressionParser.Plus); }
		public TerminalNode Plus(int i) {
			return getToken(CExpressionParser.Plus, i);
		}
		public List<TerminalNode> Minus() { return getTokens(CExpressionParser.Minus); }
		public TerminalNode Minus(int i) {
			return getToken(CExpressionParser.Minus, i);
		}
		public AdditiveExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additiveExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterAdditiveExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitAdditiveExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitAdditiveExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditiveExpressionContext additiveExpression() throws RecognitionException {
		AdditiveExpressionContext _localctx = new AdditiveExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_additiveExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			multiplicativeExpression();
			setState(67);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Plus || _la==Minus) {
				{
				{
				setState(63);
				_la = _input.LA(1);
				if ( !(_la==Plus || _la==Minus) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(64);
				multiplicativeExpression();
				}
				}
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ShiftExpressionContext extends ParserRuleContext {
		public List<AdditiveExpressionContext> additiveExpression() {
			return getRuleContexts(AdditiveExpressionContext.class);
		}
		public AdditiveExpressionContext additiveExpression(int i) {
			return getRuleContext(AdditiveExpressionContext.class,i);
		}
		public List<TerminalNode> LeftShift() { return getTokens(CExpressionParser.LeftShift); }
		public TerminalNode LeftShift(int i) {
			return getToken(CExpressionParser.LeftShift, i);
		}
		public List<TerminalNode> RightShift() { return getTokens(CExpressionParser.RightShift); }
		public TerminalNode RightShift(int i) {
			return getToken(CExpressionParser.RightShift, i);
		}
		public ShiftExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shiftExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterShiftExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitShiftExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitShiftExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShiftExpressionContext shiftExpression() throws RecognitionException {
		ShiftExpressionContext _localctx = new ShiftExpressionContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_shiftExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			additiveExpression();
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LeftShift || _la==RightShift) {
				{
				{
				setState(71);
				_la = _input.LA(1);
				if ( !(_la==LeftShift || _la==RightShift) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(72);
				additiveExpression();
				}
				}
				setState(77);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RelationalExpressionContext extends ParserRuleContext {
		public List<ShiftExpressionContext> shiftExpression() {
			return getRuleContexts(ShiftExpressionContext.class);
		}
		public ShiftExpressionContext shiftExpression(int i) {
			return getRuleContext(ShiftExpressionContext.class,i);
		}
		public List<TerminalNode> Less() { return getTokens(CExpressionParser.Less); }
		public TerminalNode Less(int i) {
			return getToken(CExpressionParser.Less, i);
		}
		public List<TerminalNode> Greater() { return getTokens(CExpressionParser.Greater); }
		public TerminalNode Greater(int i) {
			return getToken(CExpressionParser.Greater, i);
		}
		public List<TerminalNode> LessEqual() { return getTokens(CExpressionParser.LessEqual); }
		public TerminalNode LessEqual(int i) {
			return getToken(CExpressionParser.LessEqual, i);
		}
		public List<TerminalNode> GreaterEqual() { return getTokens(CExpressionParser.GreaterEqual); }
		public TerminalNode GreaterEqual(int i) {
			return getToken(CExpressionParser.GreaterEqual, i);
		}
		public RelationalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitRelationalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitRelationalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationalExpressionContext relationalExpression() throws RecognitionException {
		RelationalExpressionContext _localctx = new RelationalExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_relationalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			shiftExpression();
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1920L) != 0)) {
				{
				{
				setState(79);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1920L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(80);
				shiftExpression();
				}
				}
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EqualityExpressionContext extends ParserRuleContext {
		public List<RelationalExpressionContext> relationalExpression() {
			return getRuleContexts(RelationalExpressionContext.class);
		}
		public RelationalExpressionContext relationalExpression(int i) {
			return getRuleContext(RelationalExpressionContext.class,i);
		}
		public List<TerminalNode> Equal() { return getTokens(CExpressionParser.Equal); }
		public TerminalNode Equal(int i) {
			return getToken(CExpressionParser.Equal, i);
		}
		public List<TerminalNode> NotEqual() { return getTokens(CExpressionParser.NotEqual); }
		public TerminalNode NotEqual(int i) {
			return getToken(CExpressionParser.NotEqual, i);
		}
		public EqualityExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equalityExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterEqualityExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitEqualityExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitEqualityExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualityExpressionContext equalityExpression() throws RecognitionException {
		EqualityExpressionContext _localctx = new EqualityExpressionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_equalityExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			relationalExpression();
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Equal || _la==NotEqual) {
				{
				{
				setState(87);
				_la = _input.LA(1);
				if ( !(_la==Equal || _la==NotEqual) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(88);
				relationalExpression();
				}
				}
				setState(93);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AndExpressionContext extends ParserRuleContext {
		public List<EqualityExpressionContext> equalityExpression() {
			return getRuleContexts(EqualityExpressionContext.class);
		}
		public EqualityExpressionContext equalityExpression(int i) {
			return getRuleContext(EqualityExpressionContext.class,i);
		}
		public List<TerminalNode> And() { return getTokens(CExpressionParser.And); }
		public TerminalNode And(int i) {
			return getToken(CExpressionParser.And, i);
		}
		public AndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_andExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitAndExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndExpressionContext andExpression() throws RecognitionException {
		AndExpressionContext _localctx = new AndExpressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_andExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			equalityExpression();
			setState(99);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==And) {
				{
				{
				setState(95);
				match(And);
				setState(96);
				equalityExpression();
				}
				}
				setState(101);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExclusiveOrExpressionContext extends ParserRuleContext {
		public List<AndExpressionContext> andExpression() {
			return getRuleContexts(AndExpressionContext.class);
		}
		public AndExpressionContext andExpression(int i) {
			return getRuleContext(AndExpressionContext.class,i);
		}
		public List<TerminalNode> Caret() { return getTokens(CExpressionParser.Caret); }
		public TerminalNode Caret(int i) {
			return getToken(CExpressionParser.Caret, i);
		}
		public ExclusiveOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exclusiveOrExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterExclusiveOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitExclusiveOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitExclusiveOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExclusiveOrExpressionContext exclusiveOrExpression() throws RecognitionException {
		ExclusiveOrExpressionContext _localctx = new ExclusiveOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_exclusiveOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			andExpression();
			setState(107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Caret) {
				{
				{
				setState(103);
				match(Caret);
				setState(104);
				andExpression();
				}
				}
				setState(109);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InclusiveOrExpressionContext extends ParserRuleContext {
		public List<ExclusiveOrExpressionContext> exclusiveOrExpression() {
			return getRuleContexts(ExclusiveOrExpressionContext.class);
		}
		public ExclusiveOrExpressionContext exclusiveOrExpression(int i) {
			return getRuleContext(ExclusiveOrExpressionContext.class,i);
		}
		public List<TerminalNode> Or() { return getTokens(CExpressionParser.Or); }
		public TerminalNode Or(int i) {
			return getToken(CExpressionParser.Or, i);
		}
		public InclusiveOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inclusiveOrExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterInclusiveOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitInclusiveOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitInclusiveOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InclusiveOrExpressionContext inclusiveOrExpression() throws RecognitionException {
		InclusiveOrExpressionContext _localctx = new InclusiveOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_inclusiveOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			exclusiveOrExpression();
			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Or) {
				{
				{
				setState(111);
				match(Or);
				setState(112);
				exclusiveOrExpression();
				}
				}
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SpecialOperatorContext extends ParserRuleContext {
		public TerminalNode HasAttribute() { return getToken(CExpressionParser.HasAttribute, 0); }
		public TerminalNode LeftParen() { return getToken(CExpressionParser.LeftParen, 0); }
		public InclusiveOrExpressionContext inclusiveOrExpression() {
			return getRuleContext(InclusiveOrExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CExpressionParser.RightParen, 0); }
		public TerminalNode HasCPPAttribute() { return getToken(CExpressionParser.HasCPPAttribute, 0); }
		public TerminalNode HasCAttribute() { return getToken(CExpressionParser.HasCAttribute, 0); }
		public TerminalNode HasBuiltin() { return getToken(CExpressionParser.HasBuiltin, 0); }
		public TerminalNode HasInclude() { return getToken(CExpressionParser.HasInclude, 0); }
		public TerminalNode PathLiteral() { return getToken(CExpressionParser.PathLiteral, 0); }
		public SpecialOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterSpecialOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitSpecialOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitSpecialOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialOperatorContext specialOperator() throws RecognitionException {
		SpecialOperatorContext _localctx = new SpecialOperatorContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_specialOperator);
		int _la;
		try {
			setState(153);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case HasAttribute:
				enterOuterAlt(_localctx, 1);
				{
				setState(118);
				match(HasAttribute);
				setState(123);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(119);
					match(LeftParen);
					setState(120);
					inclusiveOrExpression();
					setState(121);
					match(RightParen);
					}
				}

				}
				break;
			case HasCPPAttribute:
				enterOuterAlt(_localctx, 2);
				{
				setState(125);
				match(HasCPPAttribute);
				setState(130);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(126);
					match(LeftParen);
					setState(127);
					inclusiveOrExpression();
					setState(128);
					match(RightParen);
					}
				}

				}
				break;
			case HasCAttribute:
				enterOuterAlt(_localctx, 3);
				{
				setState(132);
				match(HasCAttribute);
				setState(137);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(133);
					match(LeftParen);
					setState(134);
					inclusiveOrExpression();
					setState(135);
					match(RightParen);
					}
				}

				}
				break;
			case HasBuiltin:
				enterOuterAlt(_localctx, 4);
				{
				setState(139);
				match(HasBuiltin);
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(140);
					match(LeftParen);
					setState(141);
					inclusiveOrExpression();
					setState(142);
					match(RightParen);
					}
				}

				}
				break;
			case HasInclude:
				enterOuterAlt(_localctx, 5);
				{
				setState(146);
				match(HasInclude);
				setState(150);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(147);
					match(LeftParen);
					setState(148);
					match(PathLiteral);
					setState(149);
					match(RightParen);
					}
				}

				}
				break;
			case LeftParen:
			case Plus:
			case Minus:
			case Star:
			case And:
			case Not:
			case Tilde:
			case Identifier:
			case Constant:
			case StringLiteral:
				enterOuterAlt(_localctx, 6);
				{
				setState(152);
				inclusiveOrExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalAndExpressionContext extends ParserRuleContext {
		public List<SpecialOperatorContext> specialOperator() {
			return getRuleContexts(SpecialOperatorContext.class);
		}
		public SpecialOperatorContext specialOperator(int i) {
			return getRuleContext(SpecialOperatorContext.class,i);
		}
		public List<TerminalNode> AndAnd() { return getTokens(CExpressionParser.AndAnd); }
		public TerminalNode AndAnd(int i) {
			return getToken(CExpressionParser.AndAnd, i);
		}
		public LogicalAndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalAndExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterLogicalAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitLogicalAndExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitLogicalAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalAndExpressionContext logicalAndExpression() throws RecognitionException {
		LogicalAndExpressionContext _localctx = new LogicalAndExpressionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_logicalAndExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			specialOperator();
			setState(160);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AndAnd) {
				{
				{
				setState(156);
				match(AndAnd);
				setState(157);
				specialOperator();
				}
				}
				setState(162);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogicalOrExpressionContext extends ParserRuleContext {
		public List<LogicalAndExpressionContext> logicalAndExpression() {
			return getRuleContexts(LogicalAndExpressionContext.class);
		}
		public LogicalAndExpressionContext logicalAndExpression(int i) {
			return getRuleContext(LogicalAndExpressionContext.class,i);
		}
		public List<TerminalNode> OrOr() { return getTokens(CExpressionParser.OrOr); }
		public TerminalNode OrOr(int i) {
			return getToken(CExpressionParser.OrOr, i);
		}
		public LogicalOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOrExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterLogicalOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitLogicalOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitLogicalOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOrExpressionContext logicalOrExpression() throws RecognitionException {
		LogicalOrExpressionContext _localctx = new LogicalOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_logicalOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			logicalAndExpression();
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OrOr) {
				{
				{
				setState(164);
				match(OrOr);
				setState(165);
				logicalAndExpression();
				}
				}
				setState(170);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001?\u00ac\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0003\u0000#\b\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0004\u0001(\b\u0001\u000b\u0001\f\u0001)\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u00013\b\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0005\u0003:\b\u0003\n\u0003\f\u0003=\t\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0005\u0004B\b\u0004\n\u0004\f\u0004E\t\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0005\u0005J\b\u0005\n\u0005\f\u0005M\t"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006R\b\u0006\n\u0006"+
		"\f\u0006U\t\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007Z\b\u0007"+
		"\n\u0007\f\u0007]\t\u0007\u0001\b\u0001\b\u0001\b\u0005\bb\b\b\n\b\f\b"+
		"e\t\b\u0001\t\u0001\t\u0001\t\u0005\tj\b\t\n\t\f\tm\t\t\u0001\n\u0001"+
		"\n\u0001\n\u0005\nr\b\n\n\n\f\nu\t\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0003\u000b|\b\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0083\b\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u008a\b\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u0091\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u0097\b\u000b\u0001\u000b\u0003\u000b\u009a\b\u000b\u0001\f\u0001\f\u0001"+
		"\f\u0005\f\u009f\b\f\n\f\f\f\u00a2\t\f\u0001\r\u0001\r\u0001\r\u0005\r"+
		"\u00a7\b\r\n\r\f\r\u00aa\t\r\u0001\r\u0000\u0000\u000e\u0000\u0002\u0004"+
		"\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u0000\u0006\u0005"+
		"\u0000\r\r\u000f\u000f\u0011\u0011\u0014\u0014\u0019\u001a\u0001\u0000"+
		"\u0011\u0013\u0002\u0000\r\r\u000f\u000f\u0001\u0000\u000b\f\u0001\u0000"+
		"\u0007\n\u0001\u0000*+\u00b7\u0000\u001c\u0001\u0000\u0000\u0000\u0002"+
		"2\u0001\u0000\u0000\u0000\u00044\u0001\u0000\u0000\u0000\u00066\u0001"+
		"\u0000\u0000\u0000\b>\u0001\u0000\u0000\u0000\nF\u0001\u0000\u0000\u0000"+
		"\fN\u0001\u0000\u0000\u0000\u000eV\u0001\u0000\u0000\u0000\u0010^\u0001"+
		"\u0000\u0000\u0000\u0012f\u0001\u0000\u0000\u0000\u0014n\u0001\u0000\u0000"+
		"\u0000\u0016\u0099\u0001\u0000\u0000\u0000\u0018\u009b\u0001\u0000\u0000"+
		"\u0000\u001a\u00a3\u0001\u0000\u0000\u0000\u001c\"\u0003\u001a\r\u0000"+
		"\u001d\u001e\u0005\u001b\u0000\u0000\u001e\u001f\u0003\u0000\u0000\u0000"+
		"\u001f \u0005\u001c\u0000\u0000 !\u0003\u0000\u0000\u0000!#\u0001\u0000"+
		"\u0000\u0000\"\u001d\u0001\u0000\u0000\u0000\"#\u0001\u0000\u0000\u0000"+
		"#\u0001\u0001\u0000\u0000\u0000$3\u00054\u0000\u0000%3\u00055\u0000\u0000"+
		"&(\u00057\u0000\u0000\'&\u0001\u0000\u0000\u0000()\u0001\u0000\u0000\u0000"+
		")\'\u0001\u0000\u0000\u0000)*\u0001\u0000\u0000\u0000*3\u0001\u0000\u0000"+
		"\u0000+,\u0005\u0001\u0000\u0000,-\u0003\u0000\u0000\u0000-.\u0005\u0002"+
		"\u0000\u0000.3\u0001\u0000\u0000\u0000/0\u0003\u0004\u0002\u000001\u0003"+
		"\u0002\u0001\u000013\u0001\u0000\u0000\u00002$\u0001\u0000\u0000\u0000"+
		"2%\u0001\u0000\u0000\u00002\'\u0001\u0000\u0000\u00002+\u0001\u0000\u0000"+
		"\u00002/\u0001\u0000\u0000\u00003\u0003\u0001\u0000\u0000\u000045\u0007"+
		"\u0000\u0000\u00005\u0005\u0001\u0000\u0000\u00006;\u0003\u0002\u0001"+
		"\u000078\u0007\u0001\u0000\u00008:\u0003\u0002\u0001\u000097\u0001\u0000"+
		"\u0000\u0000:=\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000\u0000;<\u0001"+
		"\u0000\u0000\u0000<\u0007\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000"+
		"\u0000>C\u0003\u0006\u0003\u0000?@\u0007\u0002\u0000\u0000@B\u0003\u0006"+
		"\u0003\u0000A?\u0001\u0000\u0000\u0000BE\u0001\u0000\u0000\u0000CA\u0001"+
		"\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000D\t\u0001\u0000\u0000\u0000"+
		"EC\u0001\u0000\u0000\u0000FK\u0003\b\u0004\u0000GH\u0007\u0003\u0000\u0000"+
		"HJ\u0003\b\u0004\u0000IG\u0001\u0000\u0000\u0000JM\u0001\u0000\u0000\u0000"+
		"KI\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000L\u000b\u0001\u0000"+
		"\u0000\u0000MK\u0001\u0000\u0000\u0000NS\u0003\n\u0005\u0000OP\u0007\u0004"+
		"\u0000\u0000PR\u0003\n\u0005\u0000QO\u0001\u0000\u0000\u0000RU\u0001\u0000"+
		"\u0000\u0000SQ\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000T\r\u0001"+
		"\u0000\u0000\u0000US\u0001\u0000\u0000\u0000V[\u0003\f\u0006\u0000WX\u0007"+
		"\u0005\u0000\u0000XZ\u0003\f\u0006\u0000YW\u0001\u0000\u0000\u0000Z]\u0001"+
		"\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000"+
		"\\\u000f\u0001\u0000\u0000\u0000][\u0001\u0000\u0000\u0000^c\u0003\u000e"+
		"\u0007\u0000_`\u0005\u0014\u0000\u0000`b\u0003\u000e\u0007\u0000a_\u0001"+
		"\u0000\u0000\u0000be\u0001\u0000\u0000\u0000ca\u0001\u0000\u0000\u0000"+
		"cd\u0001\u0000\u0000\u0000d\u0011\u0001\u0000\u0000\u0000ec\u0001\u0000"+
		"\u0000\u0000fk\u0003\u0010\b\u0000gh\u0005\u0018\u0000\u0000hj\u0003\u0010"+
		"\b\u0000ig\u0001\u0000\u0000\u0000jm\u0001\u0000\u0000\u0000ki\u0001\u0000"+
		"\u0000\u0000kl\u0001\u0000\u0000\u0000l\u0013\u0001\u0000\u0000\u0000"+
		"mk\u0001\u0000\u0000\u0000ns\u0003\u0012\t\u0000op\u0005\u0015\u0000\u0000"+
		"pr\u0003\u0012\t\u0000qo\u0001\u0000\u0000\u0000ru\u0001\u0000\u0000\u0000"+
		"sq\u0001\u0000\u0000\u0000st\u0001\u0000\u0000\u0000t\u0015\u0001\u0000"+
		"\u0000\u0000us\u0001\u0000\u0000\u0000v{\u0005/\u0000\u0000wx\u0005\u0001"+
		"\u0000\u0000xy\u0003\u0014\n\u0000yz\u0005\u0002\u0000\u0000z|\u0001\u0000"+
		"\u0000\u0000{w\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|\u009a"+
		"\u0001\u0000\u0000\u0000}\u0082\u00050\u0000\u0000~\u007f\u0005\u0001"+
		"\u0000\u0000\u007f\u0080\u0003\u0014\n\u0000\u0080\u0081\u0005\u0002\u0000"+
		"\u0000\u0081\u0083\u0001\u0000\u0000\u0000\u0082~\u0001\u0000\u0000\u0000"+
		"\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u009a\u0001\u0000\u0000\u0000"+
		"\u0084\u0089\u00051\u0000\u0000\u0085\u0086\u0005\u0001\u0000\u0000\u0086"+
		"\u0087\u0003\u0014\n\u0000\u0087\u0088\u0005\u0002\u0000\u0000\u0088\u008a"+
		"\u0001\u0000\u0000\u0000\u0089\u0085\u0001\u0000\u0000\u0000\u0089\u008a"+
		"\u0001\u0000\u0000\u0000\u008a\u009a\u0001\u0000\u0000\u0000\u008b\u0090"+
		"\u00052\u0000\u0000\u008c\u008d\u0005\u0001\u0000\u0000\u008d\u008e\u0003"+
		"\u0014\n\u0000\u008e\u008f\u0005\u0002\u0000\u0000\u008f\u0091\u0001\u0000"+
		"\u0000\u0000\u0090\u008c\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000"+
		"\u0000\u0000\u0091\u009a\u0001\u0000\u0000\u0000\u0092\u0096\u00053\u0000"+
		"\u0000\u0093\u0094\u0005\u0001\u0000\u0000\u0094\u0095\u00058\u0000\u0000"+
		"\u0095\u0097\u0005\u0002\u0000\u0000\u0096\u0093\u0001\u0000\u0000\u0000"+
		"\u0096\u0097\u0001\u0000\u0000\u0000\u0097\u009a\u0001\u0000\u0000\u0000"+
		"\u0098\u009a\u0003\u0014\n\u0000\u0099v\u0001\u0000\u0000\u0000\u0099"+
		"}\u0001\u0000\u0000\u0000\u0099\u0084\u0001\u0000\u0000\u0000\u0099\u008b"+
		"\u0001\u0000\u0000\u0000\u0099\u0092\u0001\u0000\u0000\u0000\u0099\u0098"+
		"\u0001\u0000\u0000\u0000\u009a\u0017\u0001\u0000\u0000\u0000\u009b\u00a0"+
		"\u0003\u0016\u000b\u0000\u009c\u009d\u0005\u0016\u0000\u0000\u009d\u009f"+
		"\u0003\u0016\u000b\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009f\u00a2"+
		"\u0001\u0000\u0000\u0000\u00a0\u009e\u0001\u0000\u0000\u0000\u00a0\u00a1"+
		"\u0001\u0000\u0000\u0000\u00a1\u0019\u0001\u0000\u0000\u0000\u00a2\u00a0"+
		"\u0001\u0000\u0000\u0000\u00a3\u00a8\u0003\u0018\f\u0000\u00a4\u00a5\u0005"+
		"\u0017\u0000\u0000\u00a5\u00a7\u0003\u0018\f\u0000\u00a6\u00a4\u0001\u0000"+
		"\u0000\u0000\u00a7\u00aa\u0001\u0000\u0000\u0000\u00a8\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u001b\u0001\u0000"+
		"\u0000\u0000\u00aa\u00a8\u0001\u0000\u0000\u0000\u0013\")2;CKS[cks{\u0082"+
		"\u0089\u0090\u0096\u0099\u00a0\u00a8";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}