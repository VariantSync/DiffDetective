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
		HasBuiltin=50, HasInclude=51, Defined=52, Identifier=53, Constant=54, 
		DigitSequence=55, StringLiteral=56, PathLiteral=57, MultiLineMacro=58, 
		Directive=59, AsmBlock=60, Whitespace=61, Newline=62, BlockComment=63, 
		LineComment=64;
	public static final int
		RULE_conditionalExpression = 0, RULE_primaryExpression = 1, RULE_specialOperator = 2, 
		RULE_specialOperatorArgument = 3, RULE_unaryOperator = 4, RULE_multiplicativeExpression = 5, 
		RULE_additiveExpression = 6, RULE_shiftExpression = 7, RULE_relationalExpression = 8, 
		RULE_equalityExpression = 9, RULE_andExpression = 10, RULE_exclusiveOrExpression = 11, 
		RULE_inclusiveOrExpression = 12, RULE_logicalAndExpression = 13, RULE_logicalOrExpression = 14, 
		RULE_logicalOperand = 15, RULE_macroExpression = 16, RULE_argumentExpressionList = 17, 
		RULE_assignmentExpression = 18;
	private static String[] makeRuleNames() {
		return new String[] {
			"conditionalExpression", "primaryExpression", "specialOperator", "specialOperatorArgument", 
			"unaryOperator", "multiplicativeExpression", "additiveExpression", "shiftExpression", 
			"relationalExpression", "equalityExpression", "andExpression", "exclusiveOrExpression", 
			"inclusiveOrExpression", "logicalAndExpression", "logicalOrExpression", 
			"logicalOperand", "macroExpression", "argumentExpressionList", "assignmentExpression"
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
			"'__has_cpp_attribute'", "'__has_c_attribute'", "'__has_builtin'", "'__has_include'", 
			"'defined'"
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
			"Defined", "Identifier", "Constant", "DigitSequence", "StringLiteral", 
			"PathLiteral", "MultiLineMacro", "Directive", "AsmBlock", "Whitespace", 
			"Newline", "BlockComment", "LineComment"
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			logicalOrExpression();
			setState(44);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(39);
				match(Question);
				setState(40);
				conditionalExpression();
				setState(41);
				match(Colon);
				setState(42);
				conditionalExpression();
				}
				break;
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
		public MacroExpressionContext macroExpression() {
			return getRuleContext(MacroExpressionContext.class,0);
		}
		public SpecialOperatorContext specialOperator() {
			return getRuleContext(SpecialOperatorContext.class,0);
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
			setState(62);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(46);
				match(Identifier);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(47);
				match(Constant);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(49); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(48);
					match(StringLiteral);
					}
					}
					setState(51); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==StringLiteral );
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(53);
				match(LeftParen);
				setState(54);
				conditionalExpression();
				setState(55);
				match(RightParen);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(57);
				unaryOperator();
				setState(58);
				primaryExpression();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(60);
				macroExpression();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(61);
				specialOperator();
				}
				break;
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
		public SpecialOperatorArgumentContext specialOperatorArgument() {
			return getRuleContext(SpecialOperatorArgumentContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CExpressionParser.RightParen, 0); }
		public TerminalNode HasCPPAttribute() { return getToken(CExpressionParser.HasCPPAttribute, 0); }
		public TerminalNode HasCAttribute() { return getToken(CExpressionParser.HasCAttribute, 0); }
		public TerminalNode HasBuiltin() { return getToken(CExpressionParser.HasBuiltin, 0); }
		public TerminalNode HasInclude() { return getToken(CExpressionParser.HasInclude, 0); }
		public TerminalNode PathLiteral() { return getToken(CExpressionParser.PathLiteral, 0); }
		public TerminalNode Defined() { return getToken(CExpressionParser.Defined, 0); }
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
		enterRule(_localctx, 4, RULE_specialOperator);
		int _la;
		try {
			setState(109);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(64);
				match(HasAttribute);
				setState(69);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(65);
					match(LeftParen);
					setState(66);
					specialOperatorArgument();
					setState(67);
					match(RightParen);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(71);
				match(HasCPPAttribute);
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(72);
					match(LeftParen);
					setState(73);
					specialOperatorArgument();
					setState(74);
					match(RightParen);
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(78);
				match(HasCAttribute);
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(79);
					match(LeftParen);
					setState(80);
					specialOperatorArgument();
					setState(81);
					match(RightParen);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(85);
				match(HasBuiltin);
				setState(90);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(86);
					match(LeftParen);
					setState(87);
					specialOperatorArgument();
					setState(88);
					match(RightParen);
					}
				}

				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(92);
				match(HasInclude);
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(93);
					match(LeftParen);
					setState(94);
					match(PathLiteral);
					setState(95);
					match(RightParen);
					}
				}

				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(98);
				match(Defined);
				setState(103);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftParen) {
					{
					setState(99);
					match(LeftParen);
					setState(100);
					specialOperatorArgument();
					setState(101);
					match(RightParen);
					}
				}

				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(105);
				match(Defined);
				setState(107);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 17873661021126656L) != 0)) {
					{
					setState(106);
					specialOperatorArgument();
					}
				}

				}
				break;
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
	public static class SpecialOperatorArgumentContext extends ParserRuleContext {
		public TerminalNode HasAttribute() { return getToken(CExpressionParser.HasAttribute, 0); }
		public TerminalNode HasCPPAttribute() { return getToken(CExpressionParser.HasCPPAttribute, 0); }
		public TerminalNode HasCAttribute() { return getToken(CExpressionParser.HasCAttribute, 0); }
		public TerminalNode HasBuiltin() { return getToken(CExpressionParser.HasBuiltin, 0); }
		public TerminalNode HasInclude() { return getToken(CExpressionParser.HasInclude, 0); }
		public TerminalNode Defined() { return getToken(CExpressionParser.Defined, 0); }
		public TerminalNode Identifier() { return getToken(CExpressionParser.Identifier, 0); }
		public SpecialOperatorArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialOperatorArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterSpecialOperatorArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitSpecialOperatorArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitSpecialOperatorArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialOperatorArgumentContext specialOperatorArgument() throws RecognitionException {
		SpecialOperatorArgumentContext _localctx = new SpecialOperatorArgumentContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_specialOperatorArgument);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17873661021126656L) != 0)) ) {
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
		enterRule(_localctx, 8, RULE_unaryOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
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
		enterRule(_localctx, 10, RULE_multiplicativeExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			primaryExpression();
			setState(120);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(116);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 917504L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(117);
					primaryExpression();
					}
					} 
				}
				setState(122);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
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
		enterRule(_localctx, 12, RULE_additiveExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			multiplicativeExpression();
			setState(128);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(124);
					_la = _input.LA(1);
					if ( !(_la==Plus || _la==Minus) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(125);
					multiplicativeExpression();
					}
					} 
				}
				setState(130);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
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
		enterRule(_localctx, 14, RULE_shiftExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			additiveExpression();
			setState(136);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(132);
					_la = _input.LA(1);
					if ( !(_la==LeftShift || _la==RightShift) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(133);
					additiveExpression();
					}
					} 
				}
				setState(138);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
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
		enterRule(_localctx, 16, RULE_relationalExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			shiftExpression();
			setState(144);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(140);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1920L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(141);
					shiftExpression();
					}
					} 
				}
				setState(146);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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
		enterRule(_localctx, 18, RULE_equalityExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			relationalExpression();
			setState(152);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(148);
					_la = _input.LA(1);
					if ( !(_la==Equal || _la==NotEqual) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(149);
					relationalExpression();
					}
					} 
				}
				setState(154);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
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
		enterRule(_localctx, 20, RULE_andExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			equalityExpression();
			setState(160);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(156);
					match(And);
					setState(157);
					equalityExpression();
					}
					} 
				}
				setState(162);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
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
		enterRule(_localctx, 22, RULE_exclusiveOrExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			andExpression();
			setState(168);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(164);
					match(Caret);
					setState(165);
					andExpression();
					}
					} 
				}
				setState(170);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
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
		enterRule(_localctx, 24, RULE_inclusiveOrExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			exclusiveOrExpression();
			setState(176);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(172);
					match(Or);
					setState(173);
					exclusiveOrExpression();
					}
					} 
				}
				setState(178);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
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
	public static class LogicalAndExpressionContext extends ParserRuleContext {
		public List<LogicalOperandContext> logicalOperand() {
			return getRuleContexts(LogicalOperandContext.class);
		}
		public LogicalOperandContext logicalOperand(int i) {
			return getRuleContext(LogicalOperandContext.class,i);
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
		enterRule(_localctx, 26, RULE_logicalAndExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			logicalOperand();
			setState(184);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(180);
					match(AndAnd);
					setState(181);
					logicalOperand();
					}
					} 
				}
				setState(186);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
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
		enterRule(_localctx, 28, RULE_logicalOrExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			logicalAndExpression();
			setState(192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(188);
					match(OrOr);
					setState(189);
					logicalAndExpression();
					}
					} 
				}
				setState(194);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
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
	public static class LogicalOperandContext extends ParserRuleContext {
		public InclusiveOrExpressionContext inclusiveOrExpression() {
			return getRuleContext(InclusiveOrExpressionContext.class,0);
		}
		public MacroExpressionContext macroExpression() {
			return getRuleContext(MacroExpressionContext.class,0);
		}
		public LogicalOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOperand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterLogicalOperand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitLogicalOperand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitLogicalOperand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOperandContext logicalOperand() throws RecognitionException {
		LogicalOperandContext _localctx = new LogicalOperandContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_logicalOperand);
		try {
			setState(197);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(195);
				inclusiveOrExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(196);
				macroExpression();
				}
				break;
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
	public static class MacroExpressionContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CExpressionParser.Identifier, 0); }
		public TerminalNode LeftParen() { return getToken(CExpressionParser.LeftParen, 0); }
		public TerminalNode RightParen() { return getToken(CExpressionParser.RightParen, 0); }
		public ArgumentExpressionListContext argumentExpressionList() {
			return getRuleContext(ArgumentExpressionListContext.class,0);
		}
		public AssignmentExpressionContext assignmentExpression() {
			return getRuleContext(AssignmentExpressionContext.class,0);
		}
		public MacroExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterMacroExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitMacroExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitMacroExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroExpressionContext macroExpression() throws RecognitionException {
		MacroExpressionContext _localctx = new MacroExpressionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_macroExpression);
		int _la;
		try {
			setState(207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(199);
				match(Identifier);
				setState(200);
				match(LeftParen);
				setState(202);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 143974450689384450L) != 0)) {
					{
					setState(201);
					argumentExpressionList();
					}
				}

				setState(204);
				match(RightParen);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(205);
				match(Identifier);
				setState(206);
				assignmentExpression();
				}
				break;
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
	public static class ArgumentExpressionListContext extends ParserRuleContext {
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CExpressionParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CExpressionParser.Comma, i);
		}
		public ArgumentExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argumentExpressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterArgumentExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitArgumentExpressionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitArgumentExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentExpressionListContext argumentExpressionList() throws RecognitionException {
		ArgumentExpressionListContext _localctx = new ArgumentExpressionListContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_argumentExpressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			assignmentExpression();
			setState(214);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(210);
				match(Comma);
				setState(211);
				assignmentExpression();
				}
				}
				setState(216);
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
	public static class AssignmentExpressionContext extends ParserRuleContext {
		public ConditionalExpressionContext conditionalExpression() {
			return getRuleContext(ConditionalExpressionContext.class,0);
		}
		public TerminalNode DigitSequence() { return getToken(CExpressionParser.DigitSequence, 0); }
		public AssignmentExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterAssignmentExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitAssignmentExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitAssignmentExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentExpressionContext assignmentExpression() throws RecognitionException {
		AssignmentExpressionContext _localctx = new AssignmentExpressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_assignmentExpression);
		try {
			setState(219);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LeftParen:
			case Plus:
			case Minus:
			case Star:
			case And:
			case Not:
			case Tilde:
			case HasAttribute:
			case HasCPPAttribute:
			case HasCAttribute:
			case HasBuiltin:
			case HasInclude:
			case Defined:
			case Identifier:
			case Constant:
			case StringLiteral:
				enterOuterAlt(_localctx, 1);
				{
				setState(217);
				conditionalExpression();
				}
				break;
			case DigitSequence:
				enterOuterAlt(_localctx, 2);
				{
				setState(218);
				match(DigitSequence);
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

	public static final String _serializedATN =
		"\u0004\u0001@\u00de\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0003\u0000-\b\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0004\u0001"+
		"2\b\u0001\u000b\u0001\f\u00013\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001?\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002F\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0003\u0002M\b\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0003\u0002T\b\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002[\b\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002a\b\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002h\b"+
		"\u0002\u0001\u0002\u0001\u0002\u0003\u0002l\b\u0002\u0003\u0002n\b\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0005\u0005w\b\u0005\n\u0005\f\u0005z\t\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0005\u0006\u007f\b\u0006\n\u0006\f\u0006\u0082"+
		"\t\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u0087\b\u0007"+
		"\n\u0007\f\u0007\u008a\t\u0007\u0001\b\u0001\b\u0001\b\u0005\b\u008f\b"+
		"\b\n\b\f\b\u0092\t\b\u0001\t\u0001\t\u0001\t\u0005\t\u0097\b\t\n\t\f\t"+
		"\u009a\t\t\u0001\n\u0001\n\u0001\n\u0005\n\u009f\b\n\n\n\f\n\u00a2\t\n"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00a7\b\u000b\n\u000b"+
		"\f\u000b\u00aa\t\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u00af\b\f\n\f\f"+
		"\f\u00b2\t\f\u0001\r\u0001\r\u0001\r\u0005\r\u00b7\b\r\n\r\f\r\u00ba\t"+
		"\r\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u00bf\b\u000e\n\u000e"+
		"\f\u000e\u00c2\t\u000e\u0001\u000f\u0001\u000f\u0003\u000f\u00c6\b\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00cb\b\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0003\u0010\u00d0\b\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0005\u0011\u00d5\b\u0011\n\u0011\f\u0011\u00d8\t\u0011\u0001"+
		"\u0012\u0001\u0012\u0003\u0012\u00dc\b\u0012\u0001\u0012\u0000\u0000\u0013"+
		"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$\u0000\u0007\u0001\u0000/5\u0005\u0000\r\r\u000f\u000f"+
		"\u0011\u0011\u0014\u0014\u0019\u001a\u0001\u0000\u0011\u0013\u0002\u0000"+
		"\r\r\u000f\u000f\u0001\u0000\u000b\f\u0001\u0000\u0007\n\u0001\u0000*"+
		"+\u00ee\u0000&\u0001\u0000\u0000\u0000\u0002>\u0001\u0000\u0000\u0000"+
		"\u0004m\u0001\u0000\u0000\u0000\u0006o\u0001\u0000\u0000\u0000\bq\u0001"+
		"\u0000\u0000\u0000\ns\u0001\u0000\u0000\u0000\f{\u0001\u0000\u0000\u0000"+
		"\u000e\u0083\u0001\u0000\u0000\u0000\u0010\u008b\u0001\u0000\u0000\u0000"+
		"\u0012\u0093\u0001\u0000\u0000\u0000\u0014\u009b\u0001\u0000\u0000\u0000"+
		"\u0016\u00a3\u0001\u0000\u0000\u0000\u0018\u00ab\u0001\u0000\u0000\u0000"+
		"\u001a\u00b3\u0001\u0000\u0000\u0000\u001c\u00bb\u0001\u0000\u0000\u0000"+
		"\u001e\u00c5\u0001\u0000\u0000\u0000 \u00cf\u0001\u0000\u0000\u0000\""+
		"\u00d1\u0001\u0000\u0000\u0000$\u00db\u0001\u0000\u0000\u0000&,\u0003"+
		"\u001c\u000e\u0000\'(\u0005\u001b\u0000\u0000()\u0003\u0000\u0000\u0000"+
		")*\u0005\u001c\u0000\u0000*+\u0003\u0000\u0000\u0000+-\u0001\u0000\u0000"+
		"\u0000,\'\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-\u0001\u0001"+
		"\u0000\u0000\u0000.?\u00055\u0000\u0000/?\u00056\u0000\u000002\u00058"+
		"\u0000\u000010\u0001\u0000\u0000\u000023\u0001\u0000\u0000\u000031\u0001"+
		"\u0000\u0000\u000034\u0001\u0000\u0000\u00004?\u0001\u0000\u0000\u0000"+
		"56\u0005\u0001\u0000\u000067\u0003\u0000\u0000\u000078\u0005\u0002\u0000"+
		"\u00008?\u0001\u0000\u0000\u00009:\u0003\b\u0004\u0000:;\u0003\u0002\u0001"+
		"\u0000;?\u0001\u0000\u0000\u0000<?\u0003 \u0010\u0000=?\u0003\u0004\u0002"+
		"\u0000>.\u0001\u0000\u0000\u0000>/\u0001\u0000\u0000\u0000>1\u0001\u0000"+
		"\u0000\u0000>5\u0001\u0000\u0000\u0000>9\u0001\u0000\u0000\u0000><\u0001"+
		"\u0000\u0000\u0000>=\u0001\u0000\u0000\u0000?\u0003\u0001\u0000\u0000"+
		"\u0000@E\u0005/\u0000\u0000AB\u0005\u0001\u0000\u0000BC\u0003\u0006\u0003"+
		"\u0000CD\u0005\u0002\u0000\u0000DF\u0001\u0000\u0000\u0000EA\u0001\u0000"+
		"\u0000\u0000EF\u0001\u0000\u0000\u0000Fn\u0001\u0000\u0000\u0000GL\u0005"+
		"0\u0000\u0000HI\u0005\u0001\u0000\u0000IJ\u0003\u0006\u0003\u0000JK\u0005"+
		"\u0002\u0000\u0000KM\u0001\u0000\u0000\u0000LH\u0001\u0000\u0000\u0000"+
		"LM\u0001\u0000\u0000\u0000Mn\u0001\u0000\u0000\u0000NS\u00051\u0000\u0000"+
		"OP\u0005\u0001\u0000\u0000PQ\u0003\u0006\u0003\u0000QR\u0005\u0002\u0000"+
		"\u0000RT\u0001\u0000\u0000\u0000SO\u0001\u0000\u0000\u0000ST\u0001\u0000"+
		"\u0000\u0000Tn\u0001\u0000\u0000\u0000UZ\u00052\u0000\u0000VW\u0005\u0001"+
		"\u0000\u0000WX\u0003\u0006\u0003\u0000XY\u0005\u0002\u0000\u0000Y[\u0001"+
		"\u0000\u0000\u0000ZV\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000"+
		"[n\u0001\u0000\u0000\u0000\\`\u00053\u0000\u0000]^\u0005\u0001\u0000\u0000"+
		"^_\u00059\u0000\u0000_a\u0005\u0002\u0000\u0000`]\u0001\u0000\u0000\u0000"+
		"`a\u0001\u0000\u0000\u0000an\u0001\u0000\u0000\u0000bg\u00054\u0000\u0000"+
		"cd\u0005\u0001\u0000\u0000de\u0003\u0006\u0003\u0000ef\u0005\u0002\u0000"+
		"\u0000fh\u0001\u0000\u0000\u0000gc\u0001\u0000\u0000\u0000gh\u0001\u0000"+
		"\u0000\u0000hn\u0001\u0000\u0000\u0000ik\u00054\u0000\u0000jl\u0003\u0006"+
		"\u0003\u0000kj\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000ln\u0001"+
		"\u0000\u0000\u0000m@\u0001\u0000\u0000\u0000mG\u0001\u0000\u0000\u0000"+
		"mN\u0001\u0000\u0000\u0000mU\u0001\u0000\u0000\u0000m\\\u0001\u0000\u0000"+
		"\u0000mb\u0001\u0000\u0000\u0000mi\u0001\u0000\u0000\u0000n\u0005\u0001"+
		"\u0000\u0000\u0000op\u0007\u0000\u0000\u0000p\u0007\u0001\u0000\u0000"+
		"\u0000qr\u0007\u0001\u0000\u0000r\t\u0001\u0000\u0000\u0000sx\u0003\u0002"+
		"\u0001\u0000tu\u0007\u0002\u0000\u0000uw\u0003\u0002\u0001\u0000vt\u0001"+
		"\u0000\u0000\u0000wz\u0001\u0000\u0000\u0000xv\u0001\u0000\u0000\u0000"+
		"xy\u0001\u0000\u0000\u0000y\u000b\u0001\u0000\u0000\u0000zx\u0001\u0000"+
		"\u0000\u0000{\u0080\u0003\n\u0005\u0000|}\u0007\u0003\u0000\u0000}\u007f"+
		"\u0003\n\u0005\u0000~|\u0001\u0000\u0000\u0000\u007f\u0082\u0001\u0000"+
		"\u0000\u0000\u0080~\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000"+
		"\u0000\u0081\r\u0001\u0000\u0000\u0000\u0082\u0080\u0001\u0000\u0000\u0000"+
		"\u0083\u0088\u0003\f\u0006\u0000\u0084\u0085\u0007\u0004\u0000\u0000\u0085"+
		"\u0087\u0003\f\u0006\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0087\u008a"+
		"\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000\u0000\u0000\u0088\u0089"+
		"\u0001\u0000\u0000\u0000\u0089\u000f\u0001\u0000\u0000\u0000\u008a\u0088"+
		"\u0001\u0000\u0000\u0000\u008b\u0090\u0003\u000e\u0007\u0000\u008c\u008d"+
		"\u0007\u0005\u0000\u0000\u008d\u008f\u0003\u000e\u0007\u0000\u008e\u008c"+
		"\u0001\u0000\u0000\u0000\u008f\u0092\u0001\u0000\u0000\u0000\u0090\u008e"+
		"\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u0011"+
		"\u0001\u0000\u0000\u0000\u0092\u0090\u0001\u0000\u0000\u0000\u0093\u0098"+
		"\u0003\u0010\b\u0000\u0094\u0095\u0007\u0006\u0000\u0000\u0095\u0097\u0003"+
		"\u0010\b\u0000\u0096\u0094\u0001\u0000\u0000\u0000\u0097\u009a\u0001\u0000"+
		"\u0000\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000"+
		"\u0000\u0000\u0099\u0013\u0001\u0000\u0000\u0000\u009a\u0098\u0001\u0000"+
		"\u0000\u0000\u009b\u00a0\u0003\u0012\t\u0000\u009c\u009d\u0005\u0014\u0000"+
		"\u0000\u009d\u009f\u0003\u0012\t\u0000\u009e\u009c\u0001\u0000\u0000\u0000"+
		"\u009f\u00a2\u0001\u0000\u0000\u0000\u00a0\u009e\u0001\u0000\u0000\u0000"+
		"\u00a0\u00a1\u0001\u0000\u0000\u0000\u00a1\u0015\u0001\u0000\u0000\u0000"+
		"\u00a2\u00a0\u0001\u0000\u0000\u0000\u00a3\u00a8\u0003\u0014\n\u0000\u00a4"+
		"\u00a5\u0005\u0018\u0000\u0000\u00a5\u00a7\u0003\u0014\n\u0000\u00a6\u00a4"+
		"\u0001\u0000\u0000\u0000\u00a7\u00aa\u0001\u0000\u0000\u0000\u00a8\u00a6"+
		"\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u0017"+
		"\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000\u0000\u0000\u00ab\u00b0"+
		"\u0003\u0016\u000b\u0000\u00ac\u00ad\u0005\u0015\u0000\u0000\u00ad\u00af"+
		"\u0003\u0016\u000b\u0000\u00ae\u00ac\u0001\u0000\u0000\u0000\u00af\u00b2"+
		"\u0001\u0000\u0000\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000\u00b0\u00b1"+
		"\u0001\u0000\u0000\u0000\u00b1\u0019\u0001\u0000\u0000\u0000\u00b2\u00b0"+
		"\u0001\u0000\u0000\u0000\u00b3\u00b8\u0003\u001e\u000f\u0000\u00b4\u00b5"+
		"\u0005\u0016\u0000\u0000\u00b5\u00b7\u0003\u001e\u000f\u0000\u00b6\u00b4"+
		"\u0001\u0000\u0000\u0000\u00b7\u00ba\u0001\u0000\u0000\u0000\u00b8\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000\u0000\u00b9\u001b"+
		"\u0001\u0000\u0000\u0000\u00ba\u00b8\u0001\u0000\u0000\u0000\u00bb\u00c0"+
		"\u0003\u001a\r\u0000\u00bc\u00bd\u0005\u0017\u0000\u0000\u00bd\u00bf\u0003"+
		"\u001a\r\u0000\u00be\u00bc\u0001\u0000\u0000\u0000\u00bf\u00c2\u0001\u0000"+
		"\u0000\u0000\u00c0\u00be\u0001\u0000\u0000\u0000\u00c0\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c1\u001d\u0001\u0000\u0000\u0000\u00c2\u00c0\u0001\u0000"+
		"\u0000\u0000\u00c3\u00c6\u0003\u0018\f\u0000\u00c4\u00c6\u0003 \u0010"+
		"\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c4\u0001\u0000\u0000"+
		"\u0000\u00c6\u001f\u0001\u0000\u0000\u0000\u00c7\u00c8\u00055\u0000\u0000"+
		"\u00c8\u00ca\u0005\u0001\u0000\u0000\u00c9\u00cb\u0003\"\u0011\u0000\u00ca"+
		"\u00c9\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001\u0000\u0000\u0000\u00cb"+
		"\u00cc\u0001\u0000\u0000\u0000\u00cc\u00d0\u0005\u0002\u0000\u0000\u00cd"+
		"\u00ce\u00055\u0000\u0000\u00ce\u00d0\u0003$\u0012\u0000\u00cf\u00c7\u0001"+
		"\u0000\u0000\u0000\u00cf\u00cd\u0001\u0000\u0000\u0000\u00d0!\u0001\u0000"+
		"\u0000\u0000\u00d1\u00d6\u0003$\u0012\u0000\u00d2\u00d3\u0005\u001e\u0000"+
		"\u0000\u00d3\u00d5\u0003$\u0012\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000"+
		"\u00d5\u00d8\u0001\u0000\u0000\u0000\u00d6\u00d4\u0001\u0000\u0000\u0000"+
		"\u00d6\u00d7\u0001\u0000\u0000\u0000\u00d7#\u0001\u0000\u0000\u0000\u00d8"+
		"\u00d6\u0001\u0000\u0000\u0000\u00d9\u00dc\u0003\u0000\u0000\u0000\u00da"+
		"\u00dc\u00057\u0000\u0000\u00db\u00d9\u0001\u0000\u0000\u0000\u00db\u00da"+
		"\u0001\u0000\u0000\u0000\u00dc%\u0001\u0000\u0000\u0000\u001a,3>ELSZ`"+
		"gkmx\u0080\u0088\u0090\u0098\u00a0\u00a8\u00b0\u00b8\u00c0\u00c5\u00ca"+
		"\u00cf\u00d6\u00db";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}