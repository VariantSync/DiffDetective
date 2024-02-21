package org.variantsync.diffdetective.feature.jpp;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.variantsync.diffdetective.feature.BooleanAbstraction;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionParser;
import org.variantsync.diffdetective.feature.antlr.JPPExpressionVisitor;

import java.util.function.Function;

public class AbstractingJPPExpressionVisitor extends AbstractParseTreeVisitor<StringBuilder> implements JPPExpressionVisitor<StringBuilder> {
    // expression
    //    :   logicalOrExpression
    //    ;
    @Override
    public StringBuilder visitExpression(JPPExpressionParser.ExpressionContext ctx) {
        return ctx.logicalOrExpression().accept(this);
    }

    // logicalOrExpression
    //    :   logicalAndExpression (OR logicalAndExpression)*
    //    ;
    @Override
    public StringBuilder visitLogicalOrExpression(JPPExpressionParser.LogicalOrExpressionContext ctx) {
        return visitLogicalExpression(ctx,
                childExpression -> childExpression instanceof JPPExpressionParser.LogicalAndExpressionContext);
    }

    // logicalAndExpression
    //    :   primaryExpression (AND primaryExpression)*
    //    ;
    @Override
    public StringBuilder visitLogicalAndExpression(JPPExpressionParser.LogicalAndExpressionContext ctx) {
        return visitLogicalExpression(ctx,
                childExpression -> childExpression instanceof JPPExpressionParser.PrimaryExpressionContext);
    }

    // primaryExpression
    //    :   definedExpression
    //    |   undefinedExpression
    //    |   comparisonExpression
    //    ;
    @Override
    public StringBuilder visitPrimaryExpression(JPPExpressionParser.PrimaryExpressionContext ctx) {
        if (ctx.definedExpression() != null) {
            return ctx.definedExpression().accept(this);
        }
        if (ctx.undefinedExpression() != null) {
            return ctx.undefinedExpression().accept(this);
        }
        if (ctx.comparisonExpression() != null) {
            return ctx.comparisonExpression().accept(this);
        }
        throw new IllegalStateException("Unreachable code");
    }

    // comparisonExpression
    //    :   operand ((LT|GT|LEQ|GEQ|EQ|NEQ) operand)?
    //    ;
    @Override
    public StringBuilder visitComparisonExpression(JPPExpressionParser.ComparisonExpressionContext ctx) {
        return visitExpression(ctx, childExpression -> childExpression instanceof JPPExpressionParser.OperandContext);
    }

    // operand
    //    :   propertyExpression
    //    |   Constant
    //    |   StringLiteral+
    //    |   unaryOperator Constant
    //    ;
    @Override
    public StringBuilder visitOperand(JPPExpressionParser.OperandContext ctx) {
        // propertyExpression
        if (ctx.propertyExpression() != null) {
            return ctx.propertyExpression().accept(this);
        }
        // unaryOperator Constant
        if (ctx.unaryOperator() != null) {
            StringBuilder sb = ctx.unaryOperator().accept(this);
            sb.append(BooleanAbstraction.abstractAll(ctx.Constant().getText().trim()));
            return sb;
        }
        // Constant
        if (ctx.Constant() != null) {
            return new StringBuilder(BooleanAbstraction.abstractAll(ctx.Constant().getText().trim()));
        }
        // StringLiteral+
        if (!ctx.StringLiteral().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            ctx.StringLiteral().stream().map(ParseTree::getText).map(String::trim).map(BooleanAbstraction::abstractAll).forEach(sb::append);
            return sb;
        }
        // Unreachable
        throw new IllegalStateException("Unreachable code.");
    }

    // definedExpression
    //    :   'defined' '(' Identifier ')'
    //    ;
    @Override
    public StringBuilder visitDefinedExpression(JPPExpressionParser.DefinedExpressionContext ctx) {
        StringBuilder sb = new StringBuilder("DEFINED_");
        sb.append(ctx.Identifier().getText().trim());
        return sb;
    }

    // undefinedExpression
    //    :   NOT 'defined' '(' Identifier ')'
    //    ;
    @Override
    public StringBuilder visitUndefinedExpression(JPPExpressionParser.UndefinedExpressionContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append(BooleanAbstraction.U_NOT);
        sb.append("DEFINED_");
        sb.append(ctx.Identifier().getText().trim());
        return sb;
    }

    // propertyExpression
    //    :   '${' Identifier '}'
    //    ;
    @Override
    public StringBuilder visitPropertyExpression(JPPExpressionParser.PropertyExpressionContext ctx) {
        return new StringBuilder(ctx.Identifier().getText().trim());
    }

    // unaryOperator
    //    : U_PLUS
    //    | U_MINUS
    //    ;
    @Override
    public StringBuilder visitUnaryOperator(JPPExpressionParser.UnaryOperatorContext ctx) {
        switch (ctx.getText().trim()) {
            case "+" -> {
                return new StringBuilder(BooleanAbstraction.U_PLUS);
            }
            case "-" -> {
                return new StringBuilder(BooleanAbstraction.U_MINUS);
            }
        }
        throw new IllegalStateException("Unreachable code");
    }

    // logicalOrExpression
    //    :   logicalAndExpression (OR logicalAndExpression)*
    //    ;
    // logicalAndExpression
    //    :   primaryExpression (AND primaryExpression)*
    //    ;
    private StringBuilder visitLogicalExpression(ParserRuleContext expressionContext, Function<ParseTree, Boolean> instanceCheck) {
        StringBuilder sb = new StringBuilder();
        for (ParseTree subtree : expressionContext.children) {
            if (instanceCheck.apply(subtree)) {
                // logicalAndExpression | InclusiveOrExpression
                sb.append(subtree.accept(this));
            } else if (subtree instanceof TerminalNode terminal) {
                // '&&' | '||'
                switch (subtree.getText()) {
                    case "and" -> sb.append("&&");
                    case "or" -> sb.append("||");
                    default -> throw new IllegalStateException();
                }
            } else {
                // loop does not work as expected
                throw new IllegalStateException();
            }
        }
        return sb;
    }

    /**
     * Abstract all child nodes in the parse tree.
     *
     * @param expressionContext The root of the subtree to abstract
     * @param instanceCheck     A check for expected child node types
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
