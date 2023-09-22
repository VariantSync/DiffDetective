// Generated from /home/alex/programming/DiffDetective/src/main/resources/grammars/CExpression.g4 by ANTLR 4.13.1
package org.variantsync.diffdetective.feature;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;
import org.variantsync.diffdetective.feature.antlr.CExpressionVisitor;

import java.util.function.Function;

/**
 */
@SuppressWarnings("CheckReturnValue")
public class AbstractingCExpressionVisitor extends AbstractParseTreeVisitor<StringBuilder> implements CExpressionVisitor<StringBuilder> {

	public AbstractingCExpressionVisitor() {}

	// conditionalExpression
	//    :   logicalOrExpression
	//    ;
	@Override public StringBuilder visitConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx) {
		return ctx.logicalOrExpression().accept(this);
	}

	// primaryExpression
	//    :   Identifier
	//    |   Constant
	//    |   StringLiteral+
	//    |   '(' conditionalExpression ')'
	//    |   unaryOperator primaryExpression
	//    ;
	@Override public StringBuilder visitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx) {
		// Identifier
		if (ctx.Identifier() != null) {
			// Terminal
			return new StringBuilder(ctx.Identifier().getText());
		}
		// Constant
		if (ctx.Constant() != null) {
			// Terminal
			return new StringBuilder(ctx.Constant().getText());
		}
		// StringLiteral*
		if (!ctx.StringLiteral().isEmpty()) {
			// Terminal
			StringBuilder sb = new StringBuilder();
			ctx.StringLiteral().forEach(sb::append);
			return sb;
		}
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
		// For all other variants, we delegate
		throw new IllegalStateException();
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

	// logicalAndExpression
	//    :   inclusiveOrExpression ('&&' inclusiveOrExpression)*
	//    ;
	@Override public StringBuilder visitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx) {
		return visitExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.InclusiveOrExpressionContext);
	}

	// logicalOrExpression
	//    :   logicalAndExpression ( '||' logicalAndExpression)*
	//    ;
	@Override public StringBuilder visitLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx) {
		return visitExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.LogicalAndExpressionContext);
	}

	private StringBuilder visitExpression(ParserRuleContext expressionContext, Function<ParseTree, Boolean> instanceCheck) {
		StringBuilder sb = new StringBuilder();
		for (ParseTree subtree : expressionContext.children) {
			if (instanceCheck.apply(subtree)) {
				// Some operand
				sb.append(subtree.accept(this));
			} else if (subtree instanceof TerminalNode terminal) {
				// Some operator that requires abstraction
				switch (terminal.getText()) {
					case "*" -> sb.append(BooleanAbstraction.MUL);
					case "/" -> sb.append(BooleanAbstraction.DIV);
					case "%" -> sb.append(BooleanAbstraction.MOD);
					case "+" -> sb.append(BooleanAbstraction.ADD);
					case "-" -> sb.append(BooleanAbstraction.SUB);
					case "<<" -> sb.append(BooleanAbstraction.LSHIFT);
					case ">>" -> sb.append(BooleanAbstraction.RSHIFT);
					case "<" -> sb.append(BooleanAbstraction.LT);
					case ">" -> sb.append(BooleanAbstraction.GT);
					case "<=" -> sb.append(BooleanAbstraction.LEQ);
					case ">=" -> sb.append(BooleanAbstraction.GEQ);
					case "==" -> sb.append(BooleanAbstraction.EQ);
					case "!=" -> sb.append(BooleanAbstraction.NEQ);
					case "&" -> sb.append(BooleanAbstraction.AND);
					case "^" -> sb.append(BooleanAbstraction.XOR);
					case "|" -> sb.append(BooleanAbstraction.OR);
					case "&&" -> sb.append(BooleanAbstraction.L_AND);
					case "||" -> sb.append(BooleanAbstraction.L_OR);
					default -> throw new IllegalStateException();
				}
			} else {
				// loop does not work as expected
				throw new IllegalStateException();
			}
		}
		return sb;
	}
}