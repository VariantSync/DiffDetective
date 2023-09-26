package org.variantsync.diffdetective.feature;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.tinylog.Logger;
import org.variantsync.diffdetective.feature.antlr.CExpressionLexer;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;

import java.util.BitSet;
import java.util.function.Function;

/**
 * Visitor that controls how subtrees are evaluated further.
 */
@SuppressWarnings("CheckReturnValue")
public class ControllingCExpressionVisitor extends BasicCExpressionVisitor {
	private final AbstractingCExpressionVisitor abstractingVisitor = new AbstractingCExpressionVisitor();

	public ControllingCExpressionVisitor() {}

	public String simplify(String formula) {
		CExpressionLexer lexer = new CExpressionLexer(CharStreams.fromString(formula));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CExpressionParser parser = new CExpressionParser(tokens);
		parser.addErrorListener(new ANTLRErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
				Logger.warn("syntax error: {} ; {}", s, e);
				Logger.warn("formula: {}", formula);
			}

			@Override
			public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
			}

			@Override
			public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
			}

			@Override
			public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
			}
		});
		ParseTree tree = parser.expression();
		return tree.accept(this).toString();
	}

	// conditionalExpression
	//    :   logicalOrExpression ('?' expression ':' conditionalExpression)?
	//    // Capture weird concatenations that were observed in the ESEC/FSE subjects
	//    // e.g., __has_warning("-Wan-island-to-discover"_bar)
	//    |   logicalOrExpression conditionalExpression*
	//    ;
	@Override public StringBuilder visitConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx) {
		if (ctx.expression() != null || !ctx.conditionalExpression().isEmpty()) {
			// logicalOrExpression '?' expression ':' conditionalExpression
			// | logicalOrExpression conditionalExpression*
			// We have to abstract the expression if it is a ternary expression
			return ctx.accept(abstractingVisitor);
		} else {
			// logicalOrExpression
			return ctx.logicalOrExpression().accept(this);
		}
	}

	// primaryExpression
	//    :   Identifier
	//    |   Constant
	//    |   StringLiteral+
	//    |   '(' expression ')'
	//    |   unaryOperator primaryExpression
	//    |   macroExpression
	//    |   specialOperator
	//    ;
	@Override public StringBuilder visitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx) {
		// macroExpression
		if (ctx.macroExpression() != null) {
			return ctx.macroExpression().accept(abstractingVisitor);
		}

		// specialOperator
		if (ctx.specialOperator() != null) {
			return ctx.specialOperator().accept(abstractingVisitor);
		}

		// StringLiteral
		if (!ctx.StringLiteral().isEmpty()) {
			return ctx.accept(abstractingVisitor);
		}

		// For all other variants, we delegate
		return super.visitPrimaryExpression(ctx);
	}

	// namespaceExpression
	//    :   primaryExpression (':' primaryExpression)*
	//    ;
	@Override
	public StringBuilder visitNamespaceExpression(CExpressionParser.NamespaceExpressionContext ctx) {
		if (ctx.primaryExpression().size() > 1) {
			// primaryExpression (('*'|'/'|'%') primaryExpression)+
			// We have to abstract the arithmetic expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// primaryExpression
			// There is exactly one child expression
			return ctx.primaryExpression(0).accept(this);
		}
	}

	// multiplicativeExpression
	//    :   primaryExpression (('*'|'/'|'%') primaryExpression)*
	//    ;
	@Override public StringBuilder visitMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx) {
		if (ctx.namespaceExpression().size() > 1) {
			// primaryExpression (('*'|'/'|'%') primaryExpression)+
			// We have to abstract the arithmetic expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// primaryExpression
			// There is exactly one child expression
			return ctx.namespaceExpression(0).accept(this);
		}
	}

	// additiveExpression
	//    :   multiplicativeExpression (('+'|'-') multiplicativeExpression)*
	//    ;
	@Override public StringBuilder visitAdditiveExpression(CExpressionParser.AdditiveExpressionContext ctx) {
		if (ctx.multiplicativeExpression().size() > 1) {
			// multiplicativeExpression (('+'|'-') multiplicativeExpression)+
			// We have to abstract the arithmetic expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// multiplicativeExpression
			// There is exactly one child expression
			return ctx.multiplicativeExpression(0).accept(this);
		}
	}

	// shiftExpression
	//    :   additiveExpression (('<<'|'>>') additiveExpression)*
	//    ;
	@Override public StringBuilder visitShiftExpression(CExpressionParser.ShiftExpressionContext ctx) {
		if (ctx.additiveExpression().size() > 1) {
			// additiveExpression (('<<'|'>>') additiveExpression)+
			// We have to abstract the shift expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// additiveExpression
			// There is exactly one child expression
			return ctx.additiveExpression(0).accept(this);
		}
	}

	// relationalExpression
	//    :   shiftExpression (('<'|'>'|'<='|'>=') shiftExpression)*
	//    ;
	@Override public StringBuilder visitRelationalExpression(CExpressionParser.RelationalExpressionContext ctx) {
		if (ctx.shiftExpression().size() > 1) {
			// shiftExpression (('<'|'>'|'<='|'>=') shiftExpression)+
			// We have to abstract the relational expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// shiftExpression
			// There is exactly one child expression
			return ctx.shiftExpression(0).accept(this);
		}
	}

	// equalityExpression
	//    :   relationalExpression (('=='| '!=') relationalExpression)*
	//    ;
	@Override public StringBuilder visitEqualityExpression(CExpressionParser.EqualityExpressionContext ctx) {
		if (ctx.relationalExpression().size() > 1) {
			// relationalExpression (('=='| '!=') relationalExpression)+
			// We have to abstract the equality expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// relationalExpression
			// There is exactly one child expression
			return ctx.relationalExpression(0).accept(this);
		}
	}

	// specialOperator
	//    :   HasAttribute ('(' specialOperatorArgument ')')?
	//    |   HasCPPAttribute ('(' specialOperatorArgument ')')?
	//    |   HasCAttribute ('(' specialOperatorArgument ')')?
	//    |   HasBuiltin ('(' specialOperatorArgument ')')?
	//    |   HasInclude ('(' (PathLiteral | StringLiteral) ')')?
	//    |   Defined ('(' specialOperatorArgument ')')?
	//    |   Defined specialOperatorArgument?
	//    ;
	@Override public StringBuilder visitSpecialOperator(CExpressionParser.SpecialOperatorContext ctx) {
		// We have to abstract the special operator
		return ctx.accept(abstractingVisitor);
	}

	// specialOperatorArgument
	//    :   HasAttribute
	//    |   HasCPPAttribute
	//    |   HasCAttribute
	//    |   HasBuiltin
	//    |   HasInclude
	//    |   Defined
	//    |   Identifier
	//    ;
	@Override
	public StringBuilder visitSpecialOperatorArgument(CExpressionParser.SpecialOperatorArgumentContext ctx) {
		return ctx.accept(abstractingVisitor);
	}

	@Override
	public StringBuilder visitMacroExpression(CExpressionParser.MacroExpressionContext ctx) {
		return ctx.accept(abstractingVisitor);
	}

	// argumentExpressionList
	//    :   assignmentExpression (',' assignmentExpression)*
	//    |   assignmentExpression (assignmentExpression)*
	//    ;
	@Override
	public StringBuilder visitArgumentExpressionList(CExpressionParser.ArgumentExpressionListContext ctx) {
		return ctx.accept(abstractingVisitor);
	}

	// assignmentExpression
	//    :   conditionalExpression
	//    |   DigitSequence // for
	//    |   PathLiteral
	//    |   StringLiteral
	//    |   primaryExpression assignmentOperator assignmentExpression
	//    ;
	@Override
	public StringBuilder visitAssignmentExpression(CExpressionParser.AssignmentExpressionContext ctx) {
		if (ctx.conditionalExpression() != null) {
			return ctx.conditionalExpression().accept(this);
		} else {
			return ctx.accept(abstractingVisitor);
		}
	}

	// assignmentOperator
	//    :   '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
	//    ;
	@Override
	public StringBuilder visitAssignmentOperator(CExpressionParser.AssignmentOperatorContext ctx) {
		return ctx.accept(abstractingVisitor);
	}

	// expression
	//    :   assignmentExpression (',' assignmentExpression)*
	//    ;
	@Override
	public StringBuilder visitExpression(CExpressionParser.ExpressionContext ctx) {
		if (ctx.assignmentExpression().size() > 1) {
			// assignmentExpression (',' assignmentExpression)+
			return ctx.accept(abstractingVisitor);
		} else {
			// assignmentExpression
			return ctx.assignmentExpression(0).accept(this);
		}
	}

	// andExpression
	//    :   equalityExpression ( '&' equalityExpression)*
	//    ;
	@Override public StringBuilder visitAndExpression(CExpressionParser.AndExpressionContext ctx) {
		if (ctx.equalityExpression().size() > 1) {
			// equalityExpression ( '&' equalityExpression)+
			// We have to abstract the 'and' expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// equalityExpression
			// There is exactly one child expression
			return ctx.equalityExpression(0).accept(this);
		}
	}

	// exclusiveOrExpression
	//    :   andExpression ('^' andExpression)*
	//    ;
	@Override public StringBuilder visitExclusiveOrExpression(CExpressionParser.ExclusiveOrExpressionContext ctx) {
		if (ctx.andExpression().size() > 1) {
			// andExpression ('^' andExpression)+
			// We have to abstract the xor expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// andExpression
			// There is exactly one child expression
			return ctx.andExpression(0).accept(this);
		}
	}

	// inclusiveOrExpression
	//    :   exclusiveOrExpression ('|' exclusiveOrExpression)*
	//    ;
	@Override public StringBuilder visitInclusiveOrExpression(CExpressionParser.InclusiveOrExpressionContext ctx) {
		if (ctx.exclusiveOrExpression().size() > 1) {
			// exclusiveOrExpression ('|' exclusiveOrExpression)+
			// We have to abstract the 'or' expression if there is more than one operand
			return ctx.accept(abstractingVisitor);
		} else {
			// exclusiveOrExpression
			// There is exactly one child expression
			return ctx.exclusiveOrExpression(0).accept(this);
		}
	}

	// logicalAndExpression
	//    :   logicalOperand ( '&&' logicalOperand)*
	//    ;
	@Override public StringBuilder visitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx) {
		return visitLogicalExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.LogicalOperandContext);
	}

	// logicalOrExpression
	//    :   logicalAndExpression ( '||' logicalAndExpression)*
	//    ;
	@Override public StringBuilder visitLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx) {
		return visitLogicalExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.LogicalAndExpressionContext);
	}

	// logicalOperand
	//    :   inclusiveOrExpression
	//    |   macroExpression
	//    ;
	@Override
	public StringBuilder visitLogicalOperand(CExpressionParser.LogicalOperandContext ctx) {
		if (ctx.inclusiveOrExpression() != null) {
			return ctx.inclusiveOrExpression().accept(this);
		} else {
			return ctx.macroExpression().accept(abstractingVisitor);
		}
	}

	private StringBuilder visitLogicalExpression(ParserRuleContext expressionContext, Function<ParseTree, Boolean> instanceCheck) {
		StringBuilder sb = new StringBuilder();
		for (ParseTree subtree : expressionContext.children) {
			if (instanceCheck.apply(subtree)) {
				// logicalAndExpression | InclusiveOrExpression
				sb.append(subtree.accept(this));
			} else if (subtree instanceof TerminalNode terminal) {
				// '&&' | '||'
				sb.append(terminal.getText());
			} else {
				// loop does not work as expected
				throw new IllegalStateException();
			}
		}
		return sb;
	}
}