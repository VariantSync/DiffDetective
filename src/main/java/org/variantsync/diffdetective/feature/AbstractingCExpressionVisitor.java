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
	//    :   logicalOrExpression ('?' expression ':' conditionalExpression)?
	//    ;
	@Override public StringBuilder visitConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx) {
		return visitExpression(ctx,
				childContext -> childContext instanceof CExpressionParser.LogicalOrExpressionContext
						|| childContext instanceof CExpressionParser.ExpressionContext
						|| childContext instanceof CExpressionParser.ConditionalExpressionContext);
	}

	// primaryExpression
	//    :   macroExpression
	//    |	  Identifier
	//    |   Constant
	//    |   StringLiteral+
	//    |   '(' expression ')'
	//    |   unaryOperator primaryExpression
	//    |   specialOperator
	//    ;
	@Override public StringBuilder visitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx) {
		// macroExpression
		if (ctx.macroExpression() != null) {
			return ctx.macroExpression().accept(this);
		}
		// Identifier
		if (ctx.Identifier() != null) {
			// Terminal
			return new StringBuilder(BooleanAbstraction.abstractAll(ctx.Identifier().getText().trim()));
		}
		// '(' expression ')'
		if (ctx.expression() != null) {
			StringBuilder sb = ctx.expression().accept(this);
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
		// specialOperator
		if (ctx.specialOperator() != null) {
			return ctx.specialOperator().accept(this);
		}
		// StringLiteral*
		if (!ctx.StringLiteral().isEmpty()) {
			// Terminal
			StringBuilder sb = new StringBuilder();
			ctx.StringLiteral().stream().map(ParseTree::getText).map(String::trim).map(BooleanAbstraction::abstractAll).forEach(sb::append);
			return sb;
		}
		// Constant
		if (ctx.Constant() != null) {
			// Terminal
			return new StringBuilder(BooleanAbstraction.abstractAll(ctx.Constant().getText().trim()));
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

	// namespaceExpression
	//    :   primaryExpression (':' primaryExpression)*
	//    ;
	@Override
	public StringBuilder visitNamespaceExpression(CExpressionParser.NamespaceExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.PrimaryExpressionContext);
	}

	// multiplicativeExpression
	//    :   namespaceExpression (('*'|'/'|'%') namespaceExpression)*
	//    ;
	@Override public StringBuilder visitMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.NamespaceExpressionContext);
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
	//    |   Defined ('(' specialOperatorArgument ')')
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
		return new StringBuilder(BooleanAbstraction.abstractAll(ctx.getText().trim()));
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
	//    ;
	@Override
	public StringBuilder visitLogicalOperand(CExpressionParser.LogicalOperandContext ctx) {
		return ctx.inclusiveOrExpression().accept(this);
	}

	// macroExpression
	//    :   Identifier '(' argumentExpressionList? ')'
	//    ;
	@Override
	public StringBuilder visitMacroExpression(CExpressionParser.MacroExpressionContext ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append(ctx.Identifier().getText().trim().toUpperCase()).append("_");
		if (ctx.argumentExpressionList() != null) {
			sb.append(ctx.argumentExpressionList().accept(this));
		}
		return sb;
	}

	// argumentExpressionList
	//    :   assignmentExpression (',' assignmentExpression)*
	//    |   assignmentExpression (assignmentExpression)*
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
	//    |   PathLiteral
	//    |   StringLiteral
	//    |   primaryExpression assignmentOperator assignmentExpression
	//    ;
	@Override
	public StringBuilder visitAssignmentExpression(CExpressionParser.AssignmentExpressionContext ctx) {
		if (ctx.conditionalExpression() != null) {
			return ctx.conditionalExpression().accept(this);
		} else if (ctx.primaryExpression() != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(ctx.primaryExpression().accept(this));
			sb.append(ctx.assignmentOperator().accept(this));
			sb.append(ctx.assignmentExpression().accept(this));
			return sb;
		} else {
			return new StringBuilder(BooleanAbstraction.abstractAll(ctx.getText().trim()));
		}
	}

	// assignmentOperator
	//    :   '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
	//    ;
	@Override
	public StringBuilder visitAssignmentOperator(CExpressionParser.AssignmentOperatorContext ctx) {
		return new StringBuilder(BooleanAbstraction.abstractFirstOrAll(ctx.getText().trim()));
	}

	// expression
	//    :   assignmentExpression (',' assignmentExpression)*
	//    ;
	@Override
	public StringBuilder visitExpression(CExpressionParser.ExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.AssignmentExpressionContext);
	}

	private StringBuilder visitExpression(ParserRuleContext expressionContext, Function<ParseTree, Boolean> instanceCheck) {
		StringBuilder sb = new StringBuilder();
		for (ParseTree subtree : expressionContext.children) {
			if (instanceCheck.apply(subtree)) {
				// Some operand
				sb.append(subtree.accept(this));
			} else if (subtree instanceof TerminalNode terminal) {
				// Some operator that requires abstraction
				sb.append(BooleanAbstraction.abstractFirstOrAll(terminal.getText().trim()));
			} else {
				// loop does not work as expected
				throw new IllegalStateException();
			}
		}
		return sb;
	}
}