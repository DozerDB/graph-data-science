/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.gds.beta.filter.expression;

import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyAnd;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyEqual;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyGreaterThan;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyGreaterThanOrEquals;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyLessThan;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyLessThanOrEquals;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyNotEqual;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyOr;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.MyXor;
import org.neo4j.gds.beta.filter.expression.Expression.Function.Degree;
import org.neo4j.gds.beta.filter.expression.Expression.Function.MyDegree;
import org.neo4j.gds.beta.filter.expression.Expression.LeafExpression.MyVariable;
import org.neo4j.gds.beta.filter.expression.Expression.LeafExpression.Variable;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.DoubleLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.FalseLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.LongLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.MyDoubleLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.MyLongLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.MyStringLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.StringLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.TrueLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.MyHasNodeLabels;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.MyHasRelationshipTypes;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.MyNewParameter;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.MyNot;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.MyProperty;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.Property;
import org.opencypher.v9_0.ast.factory.ASTFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.neo4j.gds.core.StringSimilarity.prettySuggestions;
import static org.neo4j.gds.utils.StringFormatting.formatWithLocale;
import static org.neo4j.gds.utils.StringFormatting.toLowerCaseWithLocale;

class GdsAstFactory extends AstFactoryAdapter {

    private static final String LONG_MIN_VALUE_DECIMAL_STRING = Long.toString(Long.MIN_VALUE).substring(1);

    private final Map<String, ValueType> properties;

    GdsAstFactory(Map<String, ValueType> properties) {
        this.properties = properties;
    }

    @Override
    public Variable newVariable(InputPosition p, String name) {
        return new MyVariable(name);
    }


    @Override
    public Expression newParameter(
        InputPosition p, Variable v
    ) {
        return new MyNewParameter(v);
    }

    @Override
    public DoubleLiteral newDouble(InputPosition p, String image) {
        return new MyDoubleLiteral(Double.parseDouble(image));
    }

    @Override
    public LongLiteral newDecimalInteger(InputPosition p, String image, boolean negated) {
        try {
            long value = Long.parseLong(image);
            return new MyLongLiteral(negated ? -value : value);
        } catch (NumberFormatException e) {
            if (negated && LONG_MIN_VALUE_DECIMAL_STRING.equals(image)) {
                return new MyLongLiteral(Long.MIN_VALUE);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Expression newTrueLiteral(InputPosition p) {
        return TrueLiteral.INSTANCE;
    }

    @Override
    public Expression newFalseLiteral(InputPosition p) {
        return FalseLiteral.INSTANCE;
    }

    @Override
    public Expression newString(InputPosition p, String image) {
        return new MyStringLiteral(image);
    }

    @Override
    public Expression hasLabelsOrTypes(Expression subject, List<ASTFactory.StringPos<InputPosition>> labels) {
        if (subject instanceof Variable) {
            var variable = (Variable) subject;
            if (variable.name().equals("n")) {
                var nodeLabels = labels.stream().map(l -> l.string).map(NodeLabel::of).toList();
                return new MyHasNodeLabels(subject, nodeLabels);
            } else if (variable.name().equals("r")) {
                var relationshipTypes = labels
                    .stream()
                    .map(l -> l.string)
                    .map(RelationshipType::of)
                    .toList();
                return new MyHasRelationshipTypes(subject, relationshipTypes);
            }
            throw new IllegalArgumentException(formatWithLocale(
                "Invalid variable `%s`. Use `n` for nodes and `r` for relationships.",
                variable.name()
            ));
        }
        throw new UnsupportedOperationException(
            "Label / Type expression can only be combined with variable expressions.");
    }

    @Override
    public Property property(Expression subject, ASTFactory.StringPos<InputPosition> propertyKeyName) {
        var propertyKey = propertyKeyName.string;
        var propertyType = properties.getOrDefault(propertyKey, ValueType.UNKNOWN);

        return new MyProperty(subject, propertyKey, propertyType);
    }

    @Override
    public Expression functionInvocation(
        InputPosition p,
        List<String> namespace,
        String name,
        boolean distinct,
        List<Expression> arguments
    ) {
        if (toLowerCaseWithLocale(name).equals(Degree.NAME)) {
            var relationshipTypes = arguments
                .stream()
                .filter(expr -> {
                    if (expr instanceof StringLiteral) {
                        return true;
                    }
                    throw new IllegalArgumentException(formatWithLocale(
                        "Invalid argument for `%s`. Only strings are allowed. Got `%s`.",
                        Degree.NAME,
                        expr.prettyString()
                    ));

                })
                .map(expr -> (StringLiteral) expr)
                .map(StringLiteral::value)
                .map(RelationshipType::of)
                .collect(Collectors.toSet());

            return new MyDegree(relationshipTypes);
        }
        throw new UnsupportedOperationException(
            prettySuggestions(
                formatWithLocale("Unknown function `%s`.", name),
                name,
                Set.of(Degree.NAME)
            ));
    }

    @Override
    public Expression or(InputPosition p, Expression lhs, Expression rhs) {
        return new MyOr(lhs, rhs);
    }

    @Override
    public Expression xor(InputPosition p, Expression lhs, Expression rhs) {
        return new MyXor(lhs, rhs);
    }

    @Override
    public Expression and(InputPosition p, Expression lhs, Expression rhs) {
        return new MyAnd(lhs, rhs);
    }

    @Override
    public Expression not(Expression e) {
        return new MyNot(e);
    }

    @Override
    public Expression eq(InputPosition p, Expression lhs, Expression rhs) {
        return new MyEqual(lhs, rhs);
    }

    @Override
    public Expression neq(InputPosition p, Expression lhs, Expression rhs) {
        return new MyNotEqual(lhs, rhs);
    }

    @Override
    public Expression neq2(InputPosition p, Expression lhs, Expression rhs) {
        return new MyNotEqual(lhs, rhs);
    }

    @Override
    public Expression lte(InputPosition p, Expression lhs, Expression rhs) {
        return new MyLessThanOrEquals(lhs, rhs);
    }

    @Override
    public Expression gte(InputPosition p, Expression lhs, Expression rhs) {
        return new MyGreaterThanOrEquals(lhs, rhs);
    }

    @Override
    public Expression lt(InputPosition p, Expression lhs, Expression rhs) {
        return new MyLessThan(lhs, rhs);
    }

    @Override
    public Expression gt(InputPosition p, Expression lhs, Expression rhs) {
        return new MyGreaterThan(lhs, rhs);
    }

    @Override
    public InputPosition inputPosition(int offset, int line, int column) {
        return new InputPosition(offset, line, column);
    }
}
