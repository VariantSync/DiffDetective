package org.variantsync.diffdetective.feature;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;
import org.variantsync.diffdetective.feature.antlr.CExpressionVisitor;

import java.util.function.Function;

/**
 * Visitor that implements basic functionality for grammar rules that do not require special handling.
 */
@SuppressWarnings("CheckReturnValue")
public abstract class BasicCExpressionVisitor extends AbstractParseTreeVisitor<StringBuilder> implements CExpressionVisitor<StringBuilder> {
	public BasicCExpressionVisitor() {}

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
			sb.insert(0, "(");
			sb.append(")");
			return sb;
		}
		// unaryOperator primaryExpression
		if (ctx.unaryOperator() != null) {
			StringBuilder sb = ctx.unaryOperator().accept(this);
			sb.append(ctx.primaryExpression().accept(this));
			return sb;
		}
		// Unreachable
		throw new IllegalStateException("Unreachable code.");
	}

	// unaryOperator
	//    :   '&' | '*' | '+' | '-' | '~' | '!'
	//    ;
	@Override public StringBuilder visitUnaryOperator(CExpressionParser.UnaryOperatorContext ctx) { return new StringBuilder(ctx.getText()); }


	// logicalAndExpression
	//    :   inclusiveOrExpression ('&&' inclusiveOrExpression)*
	//    ;
	@Override public StringBuilder visitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx) {
		return visitLogicalExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.SpecialOperatorContext);
	}

	// logicalOrExpression
	//    :   logicalAndExpression ( '||' logicalAndExpression)*
	//    ;
	@Override public StringBuilder visitLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx) {
		return visitLogicalExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.LogicalAndExpressionContext);
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