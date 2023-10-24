// Generated from /home/alex/programming/DiffDetective/src/main/resources/grammars/CExpression.g4 by ANTLR 4.13.1
package org.variantsync.diffdetective.feature.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CExpressionParser}.
 */
public interface CExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(CExpressionParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(CExpressionParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#specialOperator}.
	 * @param ctx the parse tree
	 */
	void enterSpecialOperator(CExpressionParser.SpecialOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#specialOperator}.
	 * @param ctx the parse tree
	 */
	void exitSpecialOperator(CExpressionParser.SpecialOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#specialOperatorArgument}.
	 * @param ctx the parse tree
	 */
	void enterSpecialOperatorArgument(CExpressionParser.SpecialOperatorArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#specialOperatorArgument}.
	 * @param ctx the parse tree
	 */
	void exitSpecialOperatorArgument(CExpressionParser.SpecialOperatorArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOperator(CExpressionParser.UnaryOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOperator(CExpressionParser.UnaryOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#namespaceExpression}.
	 * @param ctx the parse tree
	 */
	void enterNamespaceExpression(CExpressionParser.NamespaceExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#namespaceExpression}.
	 * @param ctx the parse tree
	 */
	void exitNamespaceExpression(CExpressionParser.NamespaceExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(CExpressionParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(CExpressionParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(CExpressionParser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(CExpressionParser.ShiftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(CExpressionParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(CExpressionParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(CExpressionParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(CExpressionParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(CExpressionParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(CExpressionParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveOrExpression(CExpressionParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveOrExpression(CExpressionParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveOrExpression(CExpressionParser.InclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveOrExpression(CExpressionParser.InclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#logicalOperand}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperand(CExpressionParser.LogicalOperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#logicalOperand}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperand(CExpressionParser.LogicalOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#macroExpression}.
	 * @param ctx the parse tree
	 */
	void enterMacroExpression(CExpressionParser.MacroExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#macroExpression}.
	 * @param ctx the parse tree
	 */
	void exitMacroExpression(CExpressionParser.MacroExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentExpressionList(CExpressionParser.ArgumentExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentExpressionList(CExpressionParser.ArgumentExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpression(CExpressionParser.AssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpression(CExpressionParser.AssignmentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CExpressionParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperator(CExpressionParser.AssignmentOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CExpressionParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperator(CExpressionParser.AssignmentOperatorContext ctx);
}