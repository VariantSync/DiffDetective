package org.variantsync.diffdetective.feature;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;

import java.util.function.Function;

/**
 * Visitor that abstracts all symbols that might interfere with further formula analysis.
 */
@SuppressWarnings("CheckReturnValue")
public class AbstractingCExpressionVisitor extends BasicCExpressionVisitor {

	public AbstractingCExpressionVisitor() {}

	// conditionalExpression
	//    :   logicalOrExpression '?' conditionalExpression ':' conditionalExpression
	//    ;
	@Override public StringBuilder visitConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx) {
		return visitExpression(ctx,
				childContext -> childContext instanceof CExpressionParser.LogicalOrExpressionContext
						|| childContext instanceof CExpressionParser.ConditionalExpressionContext);
	}

	// primaryExpression
	//    :   Identifier
	//    |   Constant
	//    |   StringLiteral+
	//    |   '(' conditionalExpression ')'
	//    |   unaryOperator primaryExpression
	//    |   macroExpression
	//    |   specialOperator
	//    ;
	@Override public StringBuilder visitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx) {
		// '(' conditionalExpression ')'
		if (ctx.conditionalExpression() != null) {
			StringBuilder sb = ctx.conditionalExpression().accept(this);
			sb.insert(0, BooleanAbstraction.BRACKET_L);
			sb.append(BooleanAbstraction.BRACKET_R);
			return sb;
		}
		// unaryOperator primaryExpression
		if (ctx.unaryOperator() != null) {
			StringBuilder sb = ctx.unaryOperator().accept(this);
			sb.append(ctx.primaryExpression().accept(this));
			return sb;
		}

		// macroExpression
		if (ctx.macroExpression() != null) {
			return ctx.macroExpression().accept(this);
		}

		// specialOperator
		if (ctx.specialOperator() != null) {
			return ctx.specialOperator().accept(this);
		}

		// For all other variants, we delegate
		return super.visitPrimaryExpression(ctx);
	}

	// unaryOperator
	//    :   '&' | '*' | '+' | '-' | '~' | '!'
	//    ;
	@Override public StringBuilder visitUnaryOperator(CExpressionParser.UnaryOperatorContext ctx) {
		if (ctx.And() != null) {
			return new StringBuilder(BooleanAbstraction.U_AND);
		}
		if (ctx.Star() != null) {
			return new StringBuilder(BooleanAbstraction.U_STAR);
		}
		if (ctx.Plus() != null) {
			return new StringBuilder(BooleanAbstraction.U_PLUS);
		}
		if (ctx.Minus() != null) {
			return new StringBuilder(BooleanAbstraction.U_MINUS);
		}
		if (ctx.Tilde() != null) {
			return new StringBuilder(BooleanAbstraction.U_TILDE);
		}
		if (ctx.Not() != null) {
			return new StringBuilder(BooleanAbstraction.U_NOT);
		}
		throw new IllegalStateException();
	}

	// multiplicativeExpression
	//    :   primaryExpression (('*'|'/'|'%') primaryExpression)*
	//    ;
	@Override public StringBuilder visitMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.PrimaryExpressionContext);
	}

	// additiveExpression
	//    :   multiplicativeExpression (('+'|'-') multiplicativeExpression)*
	//    ;
	@Override public StringBuilder visitAdditiveExpression(CExpressionParser.AdditiveExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.MultiplicativeExpressionContext);
	}

	// shiftExpression
	//    :   additiveExpression (('<<'|'>>') additiveExpression)*
	//    ;
	@Override public StringBuilder visitShiftExpression(CExpressionParser.ShiftExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.AdditiveExpressionContext);
	}

	// relationalExpression
	//    :   shiftExpression (('<'|'>'|'<='|'>=') shiftExpression)*
	//    ;
	@Override public StringBuilder visitRelationalExpression(CExpressionParser.RelationalExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.ShiftExpressionContext);
	}

	// equalityExpression
	//    :   relationalExpression (('=='| '!=') relationalExpression)*
	//    ;
	@Override public StringBuilder visitEqualityExpression(CExpressionParser.EqualityExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.RelationalExpressionContext);
	}

	// andExpression
	//    :   equalityExpression ( '&' equalityExpression)*
	//    ;
	@Override public StringBuilder visitAndExpression(CExpressionParser.AndExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.EqualityExpressionContext);
	}

	// exclusiveOrExpression
	//    :   andExpression ('^' andExpression)*
	//    ;
	@Override public StringBuilder visitExclusiveOrExpression(CExpressionParser.ExclusiveOrExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.AndExpressionContext);
	}

	// inclusiveOrExpression
	//    :   exclusiveOrExpression ('|' exclusiveOrExpression)*
	//    ;
	@Override public StringBuilder visitInclusiveOrExpression(CExpressionParser.InclusiveOrExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.ExclusiveOrExpressionContext);
	}

	// specialOperator
	//    :   HasAttribute ('(' specialOperatorArgument ')')?
	//    |   HasCPPAttribute ('(' specialOperatorArgument ')')?
	//    |   HasCAttribute ('(' specialOperatorArgument ')')?
	//    |   HasBuiltin ('(' specialOperatorArgument ')')?
	//    |   HasInclude ('(' specialOperatorArgument ')')?
	//    |   Defined ('(' specialOperatorArgument ')')?
	//    |   Defined specialOperatorArgument?
	//    ;
	@Override public StringBuilder visitSpecialOperator(CExpressionParser.SpecialOperatorContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.SpecialOperatorArgumentContext);
	}

	// specialOperatorArgument
	//    :   HasAttribute
	//    |   HasCPPAttribute
	//    |   HasCAttribute
	//    |   HasBuiltin
	//    |   HasInclude
	//    |   Defined
	//    |   Identifier
	//    |   PathLiteral
	//    |   StringLiteral
	//    ;
	@Override
	public StringBuilder visitSpecialOperatorArgument(CExpressionParser.SpecialOperatorArgumentContext ctx) {
		return new StringBuilder(BooleanAbstraction.abstractAll(ctx.getText()));
	}

	// logicalAndExpression
	//    :   logicalOperand ( '&&' logicalOperand)*
	//    ;
	@Override public StringBuilder visitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx) {
		return visitExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.LogicalOperandContext);
	}

	// logicalOrExpression
	//    :   logicalAndExpression ( '||' logicalAndExpression)*
	//    ;
	@Override public StringBuilder visitLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx) {
		return visitExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.LogicalAndExpressionContext);
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
			return ctx.macroExpression().accept(this);
		}
	}

	// macroExpression
	//    :   Identifier '(' argumentExpressionList? ')'
	//    |   Identifier assignmentExpression
	//    ;
	@Override
	public StringBuilder visitMacroExpression(CExpressionParser.MacroExpressionContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append(ctx.Identifier().getText().toUpperCase()).append("_");
		if (ctx.assignmentExpression() != null) {
			sb.append(ctx.assignmentExpression().accept(this));
		} else if (ctx.argumentExpressionList() != null) {
			sb.append(ctx.argumentExpressionList().accept(this));
		}
		return sb;
	}

	// argumentExpressionList
	//    :   assignmentExpression (',' assignmentExpression)*
	//    ;
	@Override
	public StringBuilder visitArgumentExpressionList(CExpressionParser.ArgumentExpressionListContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append(BooleanAbstraction.BRACKET_L);
		for (int i = 0; i < ctx.assignmentExpression().size(); i++) {
			sb.append(ctx.assignmentExpression(i).accept(this));
			if (i < ctx.assignmentExpression().size()-1) {
				// For each ',' separating arguments
				sb.append("__");
			}
		}
		sb.append(BooleanAbstraction.BRACKET_R);
		return sb;
	}

	// assignmentExpression
	//    :   conditionalExpression
	//    |   DigitSequence // for
	//    ;
	@Override
	public StringBuilder visitAssignmentExpression(CExpressionParser.AssignmentExpressionContext ctx) {
		if (ctx.conditionalExpression() != null) {
			return ctx.conditionalExpression().accept(this);
		} else {
			return new StringBuilder(BooleanAbstraction.abstractAll(ctx.getText()));
		}
	}

	private StringBuilder visitExpression(ParserRuleContext expressionContext, Function<ParseTree, Boolean> instanceCheck) {
		StringBuilder sb = new StringBuilder();
		for (ParseTree subtree : expressionContext.children) {
			if (instanceCheck.apply(subtree)) {
				// Some operand
				sb.append(subtree.accept(this));
			} else if (subtree instanceof TerminalNode terminal) {
				// Some operator that requires abstraction
				sb.append(BooleanAbstraction.abstractFirstOrAll(terminal.getText()));
			} else {
				// loop does not work as expected
				throw new IllegalStateException();
			}
		}
		return sb;
	}
}