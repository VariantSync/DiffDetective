// Generated from /home/alex/programming/DiffDetective/src/main/resources/grammars/CExpression.g4 by ANTLR 4.13.1
package org.variantsync.diffdetective.feature;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.variantsync.diffdetective.feature.antlr.CExpressionLexer;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;
import org.variantsync.diffdetective.feature.antlr.CExpressionVisitor;

import java.util.function.Function;

/**
 */
@SuppressWarnings("CheckReturnValue")
public class CExpressionSimplifier extends AbstractParseTreeVisitor<StringBuilder> implements CExpressionVisitor<StringBuilder> {
	private final AbstractingCExpressionVisitor abstractingVisitor = new AbstractingCExpressionVisitor();

	public CExpressionSimplifier() {}

	public String simplify(String formula) {
		CExpressionLexer lexer = new CExpressionLexer(CharStreams.fromString(formula));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		CExpressionParser parser = new CExpressionParser(tokens);
		ParseTree tree = parser.conditionalExpression();
		System.out.println(tree.toStringTree(parser));
		return tree.accept(this).toString();
	}

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

	// multiplicativeExpression
	//    :   primaryExpression (('*'|'/'|'%') primaryExpression)*
	//    ;
	@Override public StringBuilder visitMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx) {
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
	//    :   inclusiveOrExpression ('&&' inclusiveOrExpression)*
	//    ;
	@Override public StringBuilder visitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx) {
		return visitLogicalExpression(ctx, childExpression -> childExpression instanceof CExpressionParser.InclusiveOrExpressionContext);
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