package org.variantsync.diffdetective.feature.jpp;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionParser;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionVisitor;

public class ControllingJPPExpressionVisitor extends AbstractParseTreeVisitor<StringBuilder> implements JPPExpressionVisitor<StringBuilder> {
    private final AbstractingJPPExpressionVisitor abstractingVisitor = new AbstractingJPPExpressionVisitor();

    // expression
    //    :   logicalOrExpression
    //    ;
    @Override
    public StringBuilder visitExpression(JPPExpressionParser.ExpressionContext ctx) {
        return null;
    }

    // logicalOrExpression
    //    :   logicalAndExpression (OR logicalAndExpression)*
    //    ;
    @Override
    public StringBuilder visitLogicalOrExpression(JPPExpressionParser.LogicalOrExpressionContext ctx) {
        return null;
    }

    // logicalAndExpression
    //    :   primaryExpression (AND primaryExpression)*
    //    ;
    @Override
    public StringBuilder visitLogicalAndExpression(JPPExpressionParser.LogicalAndExpressionContext ctx) {
        return null;
    }

    // primaryExpression
    //    :   definedExpression
    //    |   undefinedExpression
    //    |   comparisonExpression
    //    ;
    @Override
    public StringBuilder visitPrimaryExpression(JPPExpressionParser.PrimaryExpressionContext ctx) {
        return null;
    }

    // comparisonExpression
    //    :   operand ((LT|GT|LEQ|GEQ|EQ|NEQ) operand)?
    //    ;
    @Override
    public StringBuilder visitComparisonExpression(JPPExpressionParser.ComparisonExpressionContext ctx) {
        return null;
    }

    // operand
    //    :   propertyExpression
    //    |   Constant
    //    |   StringLiteral+
    //    |   unaryOperator Constant
    //    ;
    @Override
    public StringBuilder visitOperand(JPPExpressionParser.OperandContext ctx) {
        return null;
    }

    // definedExpression
    //    :   'defined' '(' Identifier ')'
    //    ;
    @Override
    public StringBuilder visitDefinedExpression(JPPExpressionParser.DefinedExpressionContext ctx) {
        return null;
    }

    // undefinedExpression
    //    :   NOT 'defined' '(' Identifier ')'
    //    ;
    @Override
    public StringBuilder visitUndefinedExpression(JPPExpressionParser.UndefinedExpressionContext ctx) {
        return null;
    }

    // propertyExpression
    //    :   '${' Identifier '}'
    //    ;
    @Override
    public StringBuilder visitPropertyExpression(JPPExpressionParser.PropertyExpressionContext ctx) {
        return null;
    }

    // unaryOperator
    //    : U_PLUS
    //    | U_MINUS
    //    ;
    @Override
    public StringBuilder visitUnaryOperator(JPPExpressionParser.UnaryOperatorContext ctx) {
        return null;
    }
}
