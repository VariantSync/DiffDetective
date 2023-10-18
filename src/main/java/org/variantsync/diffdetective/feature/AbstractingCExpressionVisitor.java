package org.variantsync.diffdetective.feature;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;
import org.variantsync.diffdetective.feature.antlr.CExpressionVisitor;

import java.util.function.Function;

/**
 * Visitor that abstracts all symbols of a formula, given as ANTLR parse tree, that might interfere with further formula analysis.
 * This visitor traverses the given tree and substitutes all formula substrings with replacements by calling {@link BooleanAbstraction}.
 *
 * <p>
 * Not all formulas or parts of a formula might require abstraction (e.g., 'A && B'). Therefore, this visitor should not be used directly.
 * Instead, you may use a {@link ControllingCExpressionVisitor} which internally uses an {@link AbstractingCExpressionVisitor}
 * to control how formulas are abstracted, and only abstracts those parts of a formula that require it.
 * </p>
 */
@SuppressWarnings("CheckReturnValue")
public class AbstractingCExpressionVisitor extends AbstractParseTreeVisitor<StringBuilder> implements CExpressionVisitor<StringBuilder> {

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
		// Constant
		if (ctx.Constant() != null) {
			// Terminal
			return new StringBuilder(BooleanAbstraction.abstractAll(ctx.Constant().getText().trim()));
		}
		// StringLiteral+
		if (!ctx.StringLiteral().isEmpty()) {
			// Terminal
			StringBuilder sb = new StringBuilder();
			ctx.StringLiteral().stream().map(ParseTree::getText).map(String::trim).map(BooleanAbstraction::abstractAll).forEach(sb::append);
			return sb;
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

		// Unreachable
		throw new IllegalStateException("Unreachable code.");
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
			// conditionalExpression
			return ctx.conditionalExpression().accept(this);
		} else if (ctx.primaryExpression() != null) {
			// primaryExpression assignmentOperator assignmentExpression
			StringBuilder sb = new StringBuilder();
			sb.append(ctx.primaryExpression().accept(this));
			sb.append(ctx.assignmentOperator().accept(this));
			sb.append(ctx.assignmentExpression().accept(this));
			return sb;
		} else {
			// all other cases require direct abstraction
			return new StringBuilder(BooleanAbstraction.abstractAll(ctx.getText().trim()));
		}
	}

	// assignmentOperator
	//    :   '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
	//    ;
	@Override
	public StringBuilder visitAssignmentOperator(CExpressionParser.AssignmentOperatorContext ctx) {
		return new StringBuilder(BooleanAbstraction.abstractToken(ctx.getText().trim()));
	}

	// expression
	//    :   assignmentExpression (',' assignmentExpression)*
	//    ;
	@Override
	public StringBuilder visitExpression(CExpressionParser.ExpressionContext ctx) {
		return visitExpression(ctx, childContext -> childContext instanceof CExpressionParser.AssignmentExpressionContext);
	}

	/**
	 * Abstract all child nodes in the parse tree.
	 * @param expressionContext The root of the subtree to abstract
	 * @param instanceCheck A check for expected child node types
	 * @return The abstracted formula of the subtree
	 */
	private StringBuilder visitExpression(ParserRuleContext expressionContext, Function<ParseTree, Boolean> instanceCheck) {
		StringBuilder sb = new StringBuilder();
		for (ParseTree subtree : expressionContext.children) {
			if (instanceCheck.apply(subtree)) {
				// Some operand (i.e., a subtree) that we have to visit
				sb.append(subtree.accept(this));
			} else if (subtree instanceof TerminalNode terminal) {
				// Some operator (i.e., a leaf node) that requires direct abstraction
				sb.append(BooleanAbstraction.abstractToken(terminal.getText().trim()));
			} else {
				// sanity check: loop does not work as expected
				throw new IllegalStateException();
			}
		}
		return sb;
	}
}