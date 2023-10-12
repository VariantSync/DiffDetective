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
		DigitSequence=55, StringLiteral=56, PathLiteral=57, NumberSign=58, AtSign=59, 
		Dollar=60, AsmBlock=61, Whitespace=62, Newline=63, BlockComment=64, OpenBlockComment=65, 
		LineComment=66;
	public static final int
		RULE_expression = 0, RULE_conditionalExpression = 1, RULE_primaryExpression = 2, 
		RULE_specialOperator = 3, RULE_specialOperatorArgument = 4, RULE_unaryOperator = 5, 
		RULE_namespaceExpression = 6, RULE_multiplicativeExpression = 7, RULE_additiveExpression = 8, 
		RULE_shiftExpression = 9, RULE_relationalExpression = 10, RULE_equalityExpression = 11, 
		RULE_andExpression = 12, RULE_exclusiveOrExpression = 13, RULE_inclusiveOrExpression = 14, 
		RULE_logicalAndExpression = 15, RULE_logicalOrExpression = 16, RULE_logicalOperand = 17, 
		RULE_macroExpression = 18, RULE_argumentExpressionList = 19, RULE_assignmentExpression = 20, 
		RULE_assignmentOperator = 21;
	private static String[] makeRuleNames() {
		return new String[] {
			"expression", "conditionalExpression", "primaryExpression", "specialOperator", 
			"specialOperatorArgument", "unaryOperator", "namespaceExpression", "multiplicativeExpression", 
			"additiveExpression", "shiftExpression", "relationalExpression", "equalityExpression", 
			"andExpression", "exclusiveOrExpression", "inclusiveOrExpression", "logicalAndExpression", 
			"logicalOrExpression", "logicalOperand", "macroExpression", "argumentExpressionList", 
			"assignmentExpression", "assignmentOperator"
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
			"'defined'", null, null, null, null, null, "'#'", "'@'", "'$'"
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
			"PathLiteral", "NumberSign", "AtSign", "Dollar", "AsmBlock", "Whitespace", 
			"Newline", "BlockComment", "OpenBlockComment", "LineComment"
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
	public static class ExpressionContext extends ParserRuleContext {
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
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			assignmentExpression();
			setState(49);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(45);
				match(Comma);
				setState(46);
				assignmentExpression();
				}
				}
				setState(51);
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
	public static class ConditionalExpressionContext extends ParserRuleContext {
		public LogicalOrExpressionContext logicalOrExpression() {
			return getRuleContext(LogicalOrExpressionContext.class,0);
		}
		public TerminalNode Question() { return getToken(CExpressionParser.Question, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Colon() { return getToken(CExpressionParser.Colon, 0); }
		public ConditionalExpressionContext conditionalExpression() {
			return getRuleContext(ConditionalExpressionContext.class,0);
		}
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
		enterRule(_localctx, 2, RULE_conditionalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			logicalOrExpression();
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Question) {
				{
				setState(53);
				match(Question);
				setState(54);
				expression();
				setState(55);
				match(Colon);
				setState(56);
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
		public MacroExpressionContext macroExpression() {
			return getRuleContext(MacroExpressionContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(CExpressionParser.Identifier, 0); }
		public TerminalNode Constant() { return getToken(CExpressionParser.Constant, 0); }
		public List<TerminalNode> StringLiteral() { return getTokens(CExpressionParser.StringLiteral); }
		public TerminalNode StringLiteral(int i) {
			return getToken(CExpressionParser.StringLiteral, i);
		}
		public TerminalNode LeftParen() { return getToken(CExpressionParser.LeftParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CExpressionParser.RightParen, 0); }
		public UnaryOperatorContext unaryOperator() {
			return getRuleContext(UnaryOperatorContext.class,0);
		}
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
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
		enterRule(_localctx, 4, RULE_primaryExpression);
		try {
			int _alt;
			setState(76);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(60);
				macroExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				match(Identifier);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(62);
				match(Constant);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(64); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(63);
						match(StringLiteral);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(66); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(68);
				match(LeftParen);
				setState(69);
				expression();
				setState(70);
				match(RightParen);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(72);
				unaryOperator();
				setState(73);
				primaryExpression();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(75);
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
		enterRule(_localctx, 6, RULE_specialOperator);
		try {
			setState(122);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(78);
				match(HasAttribute);
				setState(83);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(79);
					match(LeftParen);
					setState(80);
					specialOperatorArgument();
					setState(81);
					match(RightParen);
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				match(HasCPPAttribute);
				setState(90);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
				case 1:
					{
					setState(86);
					match(LeftParen);
					setState(87);
					specialOperatorArgument();
					setState(88);
					match(RightParen);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
				match(HasCAttribute);
				setState(97);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(93);
					match(LeftParen);
					setState(94);
					specialOperatorArgument();
					setState(95);
					match(RightParen);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(99);
				match(HasBuiltin);
				setState(104);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(100);
					match(LeftParen);
					setState(101);
					specialOperatorArgument();
					setState(102);
					match(RightParen);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(106);
				match(HasInclude);
				setState(111);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(107);
					match(LeftParen);
					setState(108);
					specialOperatorArgument();
					setState(109);
					match(RightParen);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(113);
				match(Defined);
				{
				setState(114);
				match(LeftParen);
				setState(115);
				specialOperatorArgument();
				setState(116);
				match(RightParen);
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(118);
				match(Defined);
				setState(120);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(119);
					specialOperatorArgument();
					}
					break;
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
		public TerminalNode PathLiteral() { return getToken(CExpressionParser.PathLiteral, 0); }
		public TerminalNode StringLiteral() { return getToken(CExpressionParser.StringLiteral, 0); }
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
		enterRule(_localctx, 8, RULE_specialOperatorArgument);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 234046443134910464L) != 0)) ) {
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
		enterRule(_localctx, 10, RULE_unaryOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
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
	public static class NamespaceExpressionContext extends ParserRuleContext {
		public List<PrimaryExpressionContext> primaryExpression() {
			return getRuleContexts(PrimaryExpressionContext.class);
		}
		public PrimaryExpressionContext primaryExpression(int i) {
			return getRuleContext(PrimaryExpressionContext.class,i);
		}
		public List<TerminalNode> Colon() { return getTokens(CExpressionParser.Colon); }
		public TerminalNode Colon(int i) {
			return getToken(CExpressionParser.Colon, i);
		}
		public NamespaceExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namespaceExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterNamespaceExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitNamespaceExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitNamespaceExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamespaceExpressionContext namespaceExpression() throws RecognitionException {
		NamespaceExpressionContext _localctx = new NamespaceExpressionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_namespaceExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			primaryExpression();
			setState(133);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(129);
					match(Colon);
					setState(130);
					primaryExpression();
					}
					} 
				}
				setState(135);
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
	public static class MultiplicativeExpressionContext extends ParserRuleContext {
		public List<NamespaceExpressionContext> namespaceExpression() {
			return getRuleContexts(NamespaceExpressionContext.class);
		}
		public NamespaceExpressionContext namespaceExpression(int i) {
			return getRuleContext(NamespaceExpressionContext.class,i);
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
		enterRule(_localctx, 14, RULE_multiplicativeExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			namespaceExpression();
			setState(141);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(137);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 917504L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(138);
					namespaceExpression();
					}
					} 
				}
				setState(143);
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
		enterRule(_localctx, 16, RULE_additiveExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			multiplicativeExpression();
			setState(149);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(145);
					_la = _input.LA(1);
					if ( !(_la==Plus || _la==Minus) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(146);
					multiplicativeExpression();
					}
					} 
				}
				setState(151);
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
		enterRule(_localctx, 18, RULE_shiftExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			additiveExpression();
			setState(157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LeftShift || _la==RightShift) {
				{
				{
				setState(153);
				_la = _input.LA(1);
				if ( !(_la==LeftShift || _la==RightShift) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(154);
				additiveExpression();
				}
				}
				setState(159);
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
		enterRule(_localctx, 20, RULE_relationalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			shiftExpression();
			setState(165);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1920L) != 0)) {
				{
				{
				setState(161);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1920L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(162);
				shiftExpression();
				}
				}
				setState(167);
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
		enterRule(_localctx, 22, RULE_equalityExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			relationalExpression();
			setState(173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Equal || _la==NotEqual) {
				{
				{
				setState(169);
				_la = _input.LA(1);
				if ( !(_la==Equal || _la==NotEqual) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(170);
				relationalExpression();
				}
				}
				setState(175);
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
		enterRule(_localctx, 24, RULE_andExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			equalityExpression();
			setState(181);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(177);
					match(And);
					setState(178);
					equalityExpression();
					}
					} 
				}
				setState(183);
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
		enterRule(_localctx, 26, RULE_exclusiveOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			andExpression();
			setState(189);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Caret) {
				{
				{
				setState(185);
				match(Caret);
				setState(186);
				andExpression();
				}
				}
				setState(191);
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
		enterRule(_localctx, 28, RULE_inclusiveOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
			exclusiveOrExpression();
			setState(197);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Or) {
				{
				{
				setState(193);
				match(Or);
				setState(194);
				exclusiveOrExpression();
				}
				}
				setState(199);
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
		enterRule(_localctx, 30, RULE_logicalAndExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			logicalOperand();
			setState(205);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AndAnd) {
				{
				{
				setState(201);
				match(AndAnd);
				setState(202);
				logicalOperand();
				}
				}
				setState(207);
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
		enterRule(_localctx, 32, RULE_logicalOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			logicalAndExpression();
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OrOr) {
				{
				{
				setState(209);
				match(OrOr);
				setState(210);
				logicalAndExpression();
				}
				}
				setState(215);
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
	public static class LogicalOperandContext extends ParserRuleContext {
		public InclusiveOrExpressionContext inclusiveOrExpression() {
			return getRuleContext(InclusiveOrExpressionContext.class,0);
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
		enterRule(_localctx, 34, RULE_logicalOperand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(216);
			inclusiveOrExpression();
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
		enterRule(_localctx, 36, RULE_macroExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			match(Identifier);
			setState(219);
			match(LeftParen);
			setState(221);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 288089638765240322L) != 0)) {
				{
				setState(220);
				argumentExpressionList();
				}
			}

			setState(223);
			match(RightParen);
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
		enterRule(_localctx, 38, RULE_argumentExpressionList);
		int _la;
		try {
			setState(240);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(225);
				assignmentExpression();
				setState(230);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==Comma) {
					{
					{
					setState(226);
					match(Comma);
					setState(227);
					assignmentExpression();
					}
					}
					setState(232);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(233);
				assignmentExpression();
				setState(237);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 288089638765240322L) != 0)) {
					{
					{
					setState(234);
					assignmentExpression();
					}
					}
					setState(239);
					_errHandler.sync(this);
					_la = _input.LA(1);
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
	public static class AssignmentExpressionContext extends ParserRuleContext {
		public ConditionalExpressionContext conditionalExpression() {
			return getRuleContext(ConditionalExpressionContext.class,0);
		}
		public TerminalNode DigitSequence() { return getToken(CExpressionParser.DigitSequence, 0); }
		public TerminalNode PathLiteral() { return getToken(CExpressionParser.PathLiteral, 0); }
		public TerminalNode StringLiteral() { return getToken(CExpressionParser.StringLiteral, 0); }
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public AssignmentOperatorContext assignmentOperator() {
			return getRuleContext(AssignmentOperatorContext.class,0);
		}
		public AssignmentExpressionContext assignmentExpression() {
			return getRuleContext(AssignmentExpressionContext.class,0);
		}
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
		enterRule(_localctx, 40, RULE_assignmentExpression);
		try {
			setState(250);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(242);
				conditionalExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(243);
				match(DigitSequence);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(244);
				match(PathLiteral);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(245);
				match(StringLiteral);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(246);
				primaryExpression();
				setState(247);
				assignmentOperator();
				setState(248);
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
	public static class AssignmentOperatorContext extends ParserRuleContext {
		public TerminalNode Assign() { return getToken(CExpressionParser.Assign, 0); }
		public TerminalNode StarAssign() { return getToken(CExpressionParser.StarAssign, 0); }
		public TerminalNode DivAssign() { return getToken(CExpressionParser.DivAssign, 0); }
		public TerminalNode ModAssign() { return getToken(CExpressionParser.ModAssign, 0); }
		public TerminalNode PlusAssign() { return getToken(CExpressionParser.PlusAssign, 0); }
		public TerminalNode MinusAssign() { return getToken(CExpressionParser.MinusAssign, 0); }
		public TerminalNode LeftShiftAssign() { return getToken(CExpressionParser.LeftShiftAssign, 0); }
		public TerminalNode RightShiftAssign() { return getToken(CExpressionParser.RightShiftAssign, 0); }
		public TerminalNode AndAssign() { return getToken(CExpressionParser.AndAssign, 0); }
		public TerminalNode XorAssign() { return getToken(CExpressionParser.XorAssign, 0); }
		public TerminalNode OrAssign() { return getToken(CExpressionParser.OrAssign, 0); }
		public AssignmentOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).enterAssignmentOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CExpressionListener ) ((CExpressionListener)listener).exitAssignmentOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CExpressionVisitor ) return ((CExpressionVisitor<? extends T>)visitor).visitAssignmentOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentOperatorContext assignmentOperator() throws RecognitionException {
		AssignmentOperatorContext _localctx = new AssignmentOperatorContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_assignmentOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4395899027456L) != 0)) ) {
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

	public static final String _serializedATN =
		"\u0004\u0001B\u00ff\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0005\u00000\b\u0000\n\u0000\f\u0000"+
		"3\t\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001;\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0004\u0002A\b\u0002\u000b\u0002\f\u0002B\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002M\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003T\b\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0003\u0003[\b\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003b\b\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003i\b"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003p\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0003\u0003y\b\u0003\u0003\u0003{\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0005\u0006\u0084\b\u0006\n\u0006\f\u0006\u0087\t\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u008c\b\u0007\n\u0007\f\u0007"+
		"\u008f\t\u0007\u0001\b\u0001\b\u0001\b\u0005\b\u0094\b\b\n\b\f\b\u0097"+
		"\t\b\u0001\t\u0001\t\u0001\t\u0005\t\u009c\b\t\n\t\f\t\u009f\t\t\u0001"+
		"\n\u0001\n\u0001\n\u0005\n\u00a4\b\n\n\n\f\n\u00a7\t\n\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0005\u000b\u00ac\b\u000b\n\u000b\f\u000b\u00af\t\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0005\f\u00b4\b\f\n\f\f\f\u00b7\t\f\u0001\r\u0001"+
		"\r\u0001\r\u0005\r\u00bc\b\r\n\r\f\r\u00bf\t\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0005\u000e\u00c4\b\u000e\n\u000e\f\u000e\u00c7\t\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u00cc\b\u000f\n\u000f\f\u000f"+
		"\u00cf\t\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u00d4\b"+
		"\u0010\n\u0010\f\u0010\u00d7\t\u0010\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0003\u0012\u00de\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00e5\b\u0013\n\u0013"+
		"\f\u0013\u00e8\t\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00ec\b\u0013"+
		"\n\u0013\f\u0013\u00ef\t\u0013\u0003\u0013\u00f1\b\u0013\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u00fb\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0000"+
		"\u0000\u0016\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*\u0000\b\u0002\u0000/589\u0005\u0000\r"+
		"\r\u000f\u000f\u0011\u0011\u0014\u0014\u0019\u001a\u0001\u0000\u0011\u0013"+
		"\u0002\u0000\r\r\u000f\u000f\u0001\u0000\u000b\f\u0001\u0000\u0007\n\u0001"+
		"\u0000*+\u0001\u0000\u001f)\u0110\u0000,\u0001\u0000\u0000\u0000\u0002"+
		"4\u0001\u0000\u0000\u0000\u0004L\u0001\u0000\u0000\u0000\u0006z\u0001"+
		"\u0000\u0000\u0000\b|\u0001\u0000\u0000\u0000\n~\u0001\u0000\u0000\u0000"+
		"\f\u0080\u0001\u0000\u0000\u0000\u000e\u0088\u0001\u0000\u0000\u0000\u0010"+
		"\u0090\u0001\u0000\u0000\u0000\u0012\u0098\u0001\u0000\u0000\u0000\u0014"+
		"\u00a0\u0001\u0000\u0000\u0000\u0016\u00a8\u0001\u0000\u0000\u0000\u0018"+
		"\u00b0\u0001\u0000\u0000\u0000\u001a\u00b8\u0001\u0000\u0000\u0000\u001c"+
		"\u00c0\u0001\u0000\u0000\u0000\u001e\u00c8\u0001\u0000\u0000\u0000 \u00d0"+
		"\u0001\u0000\u0000\u0000\"\u00d8\u0001\u0000\u0000\u0000$\u00da\u0001"+
		"\u0000\u0000\u0000&\u00f0\u0001\u0000\u0000\u0000(\u00fa\u0001\u0000\u0000"+
		"\u0000*\u00fc\u0001\u0000\u0000\u0000,1\u0003(\u0014\u0000-.\u0005\u001e"+
		"\u0000\u0000.0\u0003(\u0014\u0000/-\u0001\u0000\u0000\u000003\u0001\u0000"+
		"\u0000\u00001/\u0001\u0000\u0000\u000012\u0001\u0000\u0000\u00002\u0001"+
		"\u0001\u0000\u0000\u000031\u0001\u0000\u0000\u00004:\u0003 \u0010\u0000"+
		"56\u0005\u001b\u0000\u000067\u0003\u0000\u0000\u000078\u0005\u001c\u0000"+
		"\u000089\u0003\u0002\u0001\u00009;\u0001\u0000\u0000\u0000:5\u0001\u0000"+
		"\u0000\u0000:;\u0001\u0000\u0000\u0000;\u0003\u0001\u0000\u0000\u0000"+
		"<M\u0003$\u0012\u0000=M\u00055\u0000\u0000>M\u00056\u0000\u0000?A\u0005"+
		"8\u0000\u0000@?\u0001\u0000\u0000\u0000AB\u0001\u0000\u0000\u0000B@\u0001"+
		"\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000CM\u0001\u0000\u0000\u0000"+
		"DE\u0005\u0001\u0000\u0000EF\u0003\u0000\u0000\u0000FG\u0005\u0002\u0000"+
		"\u0000GM\u0001\u0000\u0000\u0000HI\u0003\n\u0005\u0000IJ\u0003\u0004\u0002"+
		"\u0000JM\u0001\u0000\u0000\u0000KM\u0003\u0006\u0003\u0000L<\u0001\u0000"+
		"\u0000\u0000L=\u0001\u0000\u0000\u0000L>\u0001\u0000\u0000\u0000L@\u0001"+
		"\u0000\u0000\u0000LD\u0001\u0000\u0000\u0000LH\u0001\u0000\u0000\u0000"+
		"LK\u0001\u0000\u0000\u0000M\u0005\u0001\u0000\u0000\u0000NS\u0005/\u0000"+
		"\u0000OP\u0005\u0001\u0000\u0000PQ\u0003\b\u0004\u0000QR\u0005\u0002\u0000"+
		"\u0000RT\u0001\u0000\u0000\u0000SO\u0001\u0000\u0000\u0000ST\u0001\u0000"+
		"\u0000\u0000T{\u0001\u0000\u0000\u0000UZ\u00050\u0000\u0000VW\u0005\u0001"+
		"\u0000\u0000WX\u0003\b\u0004\u0000XY\u0005\u0002\u0000\u0000Y[\u0001\u0000"+
		"\u0000\u0000ZV\u0001\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[{\u0001"+
		"\u0000\u0000\u0000\\a\u00051\u0000\u0000]^\u0005\u0001\u0000\u0000^_\u0003"+
		"\b\u0004\u0000_`\u0005\u0002\u0000\u0000`b\u0001\u0000\u0000\u0000a]\u0001"+
		"\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000b{\u0001\u0000\u0000\u0000"+
		"ch\u00052\u0000\u0000de\u0005\u0001\u0000\u0000ef\u0003\b\u0004\u0000"+
		"fg\u0005\u0002\u0000\u0000gi\u0001\u0000\u0000\u0000hd\u0001\u0000\u0000"+
		"\u0000hi\u0001\u0000\u0000\u0000i{\u0001\u0000\u0000\u0000jo\u00053\u0000"+
		"\u0000kl\u0005\u0001\u0000\u0000lm\u0003\b\u0004\u0000mn\u0005\u0002\u0000"+
		"\u0000np\u0001\u0000\u0000\u0000ok\u0001\u0000\u0000\u0000op\u0001\u0000"+
		"\u0000\u0000p{\u0001\u0000\u0000\u0000qr\u00054\u0000\u0000rs\u0005\u0001"+
		"\u0000\u0000st\u0003\b\u0004\u0000tu\u0005\u0002\u0000\u0000u{\u0001\u0000"+
		"\u0000\u0000vx\u00054\u0000\u0000wy\u0003\b\u0004\u0000xw\u0001\u0000"+
		"\u0000\u0000xy\u0001\u0000\u0000\u0000y{\u0001\u0000\u0000\u0000zN\u0001"+
		"\u0000\u0000\u0000zU\u0001\u0000\u0000\u0000z\\\u0001\u0000\u0000\u0000"+
		"zc\u0001\u0000\u0000\u0000zj\u0001\u0000\u0000\u0000zq\u0001\u0000\u0000"+
		"\u0000zv\u0001\u0000\u0000\u0000{\u0007\u0001\u0000\u0000\u0000|}\u0007"+
		"\u0000\u0000\u0000}\t\u0001\u0000\u0000\u0000~\u007f\u0007\u0001\u0000"+
		"\u0000\u007f\u000b\u0001\u0000\u0000\u0000\u0080\u0085\u0003\u0004\u0002"+
		"\u0000\u0081\u0082\u0005\u001c\u0000\u0000\u0082\u0084\u0003\u0004\u0002"+
		"\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0084\u0087\u0001\u0000\u0000"+
		"\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0085\u0086\u0001\u0000\u0000"+
		"\u0000\u0086\r\u0001\u0000\u0000\u0000\u0087\u0085\u0001\u0000\u0000\u0000"+
		"\u0088\u008d\u0003\f\u0006\u0000\u0089\u008a\u0007\u0002\u0000\u0000\u008a"+
		"\u008c\u0003\f\u0006\u0000\u008b\u0089\u0001\u0000\u0000\u0000\u008c\u008f"+
		"\u0001\u0000\u0000\u0000\u008d\u008b\u0001\u0000\u0000\u0000\u008d\u008e"+
		"\u0001\u0000\u0000\u0000\u008e\u000f\u0001\u0000\u0000\u0000\u008f\u008d"+
		"\u0001\u0000\u0000\u0000\u0090\u0095\u0003\u000e\u0007\u0000\u0091\u0092"+
		"\u0007\u0003\u0000\u0000\u0092\u0094\u0003\u000e\u0007\u0000\u0093\u0091"+
		"\u0001\u0000\u0000\u0000\u0094\u0097\u0001\u0000\u0000\u0000\u0095\u0093"+
		"\u0001\u0000\u0000\u0000\u0095\u0096\u0001\u0000\u0000\u0000\u0096\u0011"+
		"\u0001\u0000\u0000\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0098\u009d"+
		"\u0003\u0010\b\u0000\u0099\u009a\u0007\u0004\u0000\u0000\u009a\u009c\u0003"+
		"\u0010\b\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009c\u009f\u0001\u0000"+
		"\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000"+
		"\u0000\u0000\u009e\u0013\u0001\u0000\u0000\u0000\u009f\u009d\u0001\u0000"+
		"\u0000\u0000\u00a0\u00a5\u0003\u0012\t\u0000\u00a1\u00a2\u0007\u0005\u0000"+
		"\u0000\u00a2\u00a4\u0003\u0012\t\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000"+
		"\u00a4\u00a7\u0001\u0000\u0000\u0000\u00a5\u00a3\u0001\u0000\u0000\u0000"+
		"\u00a5\u00a6\u0001\u0000\u0000\u0000\u00a6\u0015\u0001\u0000\u0000\u0000"+
		"\u00a7\u00a5\u0001\u0000\u0000\u0000\u00a8\u00ad\u0003\u0014\n\u0000\u00a9"+
		"\u00aa\u0007\u0006\u0000\u0000\u00aa\u00ac\u0003\u0014\n\u0000\u00ab\u00a9"+
		"\u0001\u0000\u0000\u0000\u00ac\u00af\u0001\u0000\u0000\u0000\u00ad\u00ab"+
		"\u0001\u0000\u0000\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u0017"+
		"\u0001\u0000\u0000\u0000\u00af\u00ad\u0001\u0000\u0000\u0000\u00b0\u00b5"+
		"\u0003\u0016\u000b\u0000\u00b1\u00b2\u0005\u0014\u0000\u0000\u00b2\u00b4"+
		"\u0003\u0016\u000b\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b4\u00b7"+
		"\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b5\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b6\u0019\u0001\u0000\u0000\u0000\u00b7\u00b5"+
		"\u0001\u0000\u0000\u0000\u00b8\u00bd\u0003\u0018\f\u0000\u00b9\u00ba\u0005"+
		"\u0018\u0000\u0000\u00ba\u00bc\u0003\u0018\f\u0000\u00bb\u00b9\u0001\u0000"+
		"\u0000\u0000\u00bc\u00bf\u0001\u0000\u0000\u0000\u00bd\u00bb\u0001\u0000"+
		"\u0000\u0000\u00bd\u00be\u0001\u0000\u0000\u0000\u00be\u001b\u0001\u0000"+
		"\u0000\u0000\u00bf\u00bd\u0001\u0000\u0000\u0000\u00c0\u00c5\u0003\u001a"+
		"\r\u0000\u00c1\u00c2\u0005\u0015\u0000\u0000\u00c2\u00c4\u0003\u001a\r"+
		"\u0000\u00c3\u00c1\u0001\u0000\u0000\u0000\u00c4\u00c7\u0001\u0000\u0000"+
		"\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c6\u0001\u0000\u0000"+
		"\u0000\u00c6\u001d\u0001\u0000\u0000\u0000\u00c7\u00c5\u0001\u0000\u0000"+
		"\u0000\u00c8\u00cd\u0003\"\u0011\u0000\u00c9\u00ca\u0005\u0016\u0000\u0000"+
		"\u00ca\u00cc\u0003\"\u0011\u0000\u00cb\u00c9\u0001\u0000\u0000\u0000\u00cc"+
		"\u00cf\u0001\u0000\u0000\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00cd"+
		"\u00ce\u0001\u0000\u0000\u0000\u00ce\u001f\u0001\u0000\u0000\u0000\u00cf"+
		"\u00cd\u0001\u0000\u0000\u0000\u00d0\u00d5\u0003\u001e\u000f\u0000\u00d1"+
		"\u00d2\u0005\u0017\u0000\u0000\u00d2\u00d4\u0003\u001e\u000f\u0000\u00d3"+
		"\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d7\u0001\u0000\u0000\u0000\u00d5"+
		"\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000\u0000\u00d6"+
		"!\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001\u0000\u0000\u0000\u00d8\u00d9"+
		"\u0003\u001c\u000e\u0000\u00d9#\u0001\u0000\u0000\u0000\u00da\u00db\u0005"+
		"5\u0000\u0000\u00db\u00dd\u0005\u0001\u0000\u0000\u00dc\u00de\u0003&\u0013"+
		"\u0000\u00dd\u00dc\u0001\u0000\u0000\u0000\u00dd\u00de\u0001\u0000\u0000"+
		"\u0000\u00de\u00df\u0001\u0000\u0000\u0000\u00df\u00e0\u0005\u0002\u0000"+
		"\u0000\u00e0%\u0001\u0000\u0000\u0000\u00e1\u00e6\u0003(\u0014\u0000\u00e2"+
		"\u00e3\u0005\u001e\u0000\u0000\u00e3\u00e5\u0003(\u0014\u0000\u00e4\u00e2"+
		"\u0001\u0000\u0000\u0000\u00e5\u00e8\u0001\u0000\u0000\u0000\u00e6\u00e4"+
		"\u0001\u0000\u0000\u0000\u00e6\u00e7\u0001\u0000\u0000\u0000\u00e7\u00f1"+
		"\u0001\u0000\u0000\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e9\u00ed"+
		"\u0003(\u0014\u0000\u00ea\u00ec\u0003(\u0014\u0000\u00eb\u00ea\u0001\u0000"+
		"\u0000\u0000\u00ec\u00ef\u0001\u0000\u0000\u0000\u00ed\u00eb\u0001\u0000"+
		"\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u00f1\u0001\u0000"+
		"\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00e1\u0001\u0000"+
		"\u0000\u0000\u00f0\u00e9\u0001\u0000\u0000\u0000\u00f1\'\u0001\u0000\u0000"+
		"\u0000\u00f2\u00fb\u0003\u0002\u0001\u0000\u00f3\u00fb\u00057\u0000\u0000"+
		"\u00f4\u00fb\u00059\u0000\u0000\u00f5\u00fb\u00058\u0000\u0000\u00f6\u00f7"+
		"\u0003\u0004\u0002\u0000\u00f7\u00f8\u0003*\u0015\u0000\u00f8\u00f9\u0003"+
		"(\u0014\u0000\u00f9\u00fb\u0001\u0000\u0000\u0000\u00fa\u00f2\u0001\u0000"+
		"\u0000\u0000\u00fa\u00f3\u0001\u0000\u0000\u0000\u00fa\u00f4\u0001\u0000"+
		"\u0000\u0000\u00fa\u00f5\u0001\u0000\u0000\u0000\u00fa\u00f6\u0001\u0000"+
		"\u0000\u0000\u00fb)\u0001\u0000\u0000\u0000\u00fc\u00fd\u0007\u0007\u0000"+
		"\u0000\u00fd+\u0001\u0000\u0000\u0000\u001b1:BLSZahoxz\u0085\u008d\u0095"+
		"\u009d\u00a5\u00ad\u00b5\u00bd\u00c5\u00cd\u00d5\u00dd\u00e6\u00ed\u00f0"+
		"\u00fa";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}