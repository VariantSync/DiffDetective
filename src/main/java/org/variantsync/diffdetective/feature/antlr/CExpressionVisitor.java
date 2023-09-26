// Generated from /home/alex/programming/DiffDetective/src/main/resources/grammars/CExpression.g4 by ANTLR 4.13.1
package org.variantsync.diffdetective.feature.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#conditionalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#specialOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialOperator(CExpressionParser.SpecialOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#specialOperatorArgument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialOperatorArgument(CExpressionParser.SpecialOperatorArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#unaryOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOperator(CExpressionParser.UnaryOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#namespaceExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamespaceExpression(CExpressionParser.NamespaceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(CExpressionParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#shiftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(CExpressionParser.ShiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(CExpressionParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(CExpressionParser.EqualityExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(CExpressionParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusiveOrExpression(CExpressionParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclusiveOrExpression(CExpressionParser.InclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#logicalOperand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperand(CExpressionParser.LogicalOperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#macroExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroExpression(CExpressionParser.MacroExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentExpressionList(CExpressionParser.ArgumentExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpression(CExpressionParser.AssignmentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(CExpressionParser.AssignmentOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(CExpressionParser.ExpressionContext ctx);
}