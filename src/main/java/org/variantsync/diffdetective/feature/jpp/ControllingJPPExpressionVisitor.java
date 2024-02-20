package org.variantsync.diffdetective.feature.jpp;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionParser;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionVisitor;

public class ControllingJPPExpressionVisitor extends AbstractParseTreeVisitor<StringBuilder> implements JPPExpressionVisitor<StringBuilder> {
    private final AbstractingJPPExpressionVisitor abstractingVisitor = new AbstractingJPPExpressionVisitor();

    @Override
    public StringBuilder visitExpression(JPPExpressionParser.ExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitLogicalOrExpression(JPPExpressionParser.LogicalOrExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitLogicalAndExpression(JPPExpressionParser.LogicalAndExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitPrimaryExpression(JPPExpressionParser.PrimaryExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitComparisonExpression(JPPExpressionParser.ComparisonExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitOperand(JPPExpressionParser.OperandContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitDefinedExpression(JPPExpressionParser.DefinedExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitUndefinedExpression(JPPExpressionParser.UndefinedExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitPropertyExpression(JPPExpressionParser.PropertyExpressionContext ctx) {
        return null;
    }

    @Override
    public StringBuilder visitUnaryOperator(JPPExpressionParser.UnaryOperatorContext ctx) {
        return null;
    }
}
