package org.variantsync.diffdetective.feature.jpp;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.variantsync.diffdetective.feature.antlr.CExpressionParser;
import org.variantsync.diffdetective.feature.antlr.CExpressionVisitor;

public class AbstractingJPPExpressionVisitor extends AbstractParseTreeVisitor<StringBuilder> implements CExpressionVisitor<StringBuilder> {
    @Override
    public StringBuilder visitExpression(CExpressionParser.ExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitConditionalExpression(CExpressionParser.ConditionalExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitPrimaryExpression(CExpressionParser.PrimaryExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitSpecialOperator(CExpressionParser.SpecialOperatorContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitSpecialOperatorArgument(CExpressionParser.SpecialOperatorArgumentContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitUnaryOperator(CExpressionParser.UnaryOperatorContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitNamespaceExpression(CExpressionParser.NamespaceExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitMultiplicativeExpression(CExpressionParser.MultiplicativeExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitAdditiveExpression(CExpressionParser.AdditiveExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitShiftExpression(CExpressionParser.ShiftExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitRelationalExpression(CExpressionParser.RelationalExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitEqualityExpression(CExpressionParser.EqualityExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitAndExpression(CExpressionParser.AndExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitExclusiveOrExpression(CExpressionParser.ExclusiveOrExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitInclusiveOrExpression(CExpressionParser.InclusiveOrExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitLogicalAndExpression(CExpressionParser.LogicalAndExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitLogicalOrExpression(CExpressionParser.LogicalOrExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitLogicalOperand(CExpressionParser.LogicalOperandContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitMacroExpression(CExpressionParser.MacroExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitArgumentExpressionList(CExpressionParser.ArgumentExpressionListContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitAssignmentExpression(CExpressionParser.AssignmentExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitAssignmentOperator(CExpressionParser.AssignmentOperatorContext ctx) {
        return null;
    }
}
