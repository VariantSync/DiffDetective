package org.variantsync.diffdetective.feature;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;
import org.variantsync.diffdetective.feature.antlr.CExpressionVisitor;

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
	//    |   '(' expression ')'
	//    |   unaryOperator primaryExpression
	//    |   macroExpression
	//    |   specialOperator
	//    ;
	@Override public StringBuilder visitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx) {
		// Identifier
		if (ctx.Identifier() != null) {
			// Terminal
			return new StringBuilder(ctx.Identifier().getText().trim());
		}
		// Constant
		if (ctx.Constant() != null) {
			// Terminal
			return new StringBuilder(ctx.Constant().getText().trim());
		}
		// '(' conditionalExpression ')'
		if (ctx.expression() != null) {
			StringBuilder sb = ctx.expression().accept(this);
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

}