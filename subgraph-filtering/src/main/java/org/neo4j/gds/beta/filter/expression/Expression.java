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
import org.neo4j.gds.utils.StringJoining;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.neo4j.gds.core.StringSimilarity.prettySuggestions;
import static org.neo4j.gds.utils.StringFormatting.formatWithLocale;

public interface Expression {
    double TRUE = 1.0D;
    double FALSE = 0.0D;
    double EPSILON = 1E-5;
    double VARIABLE = Double.NaN;

    double evaluate(EvaluationContext context);

    default String prettyString() {
        return toString();
    }

    default ValidationContext validate(ValidationContext context) {
        return context;
    }

    default ValueType valueType() {
        return ValueType.DOUBLE;
    }

    interface LeafExpression extends Expression {

        interface Variable extends LeafExpression {

            String name();

            @Override
            default double evaluate(EvaluationContext context) {
                return VARIABLE;
            }

            @Override
            default ValidationContext validate(ValidationContext context) {
                if (context.context() == ValidationContext.Context.NODE) {
                    if (!name().equals("n")) {
                        return context.withError(new SemanticErrors.SemanticError(formatWithLocale(
                            "Invalid variable `%s`. Only `n` is allowed for nodes",
                            name()
                        )));
                    }
                } else if (context.context() == ValidationContext.Context.RELATIONSHIP) {
                    if (!name().equals("r")) {
                        return context.withError(new SemanticErrors.SemanticError(formatWithLocale(
                            "Invalid variable `%s`. Only `r` is allowed for relationships",
                            name()
                        )));
                    }
                }

                return context;
            }

            @Override
            default String prettyString() {
                return name();
            }
        }

        record MyVariable(String name) implements Variable {
            @Override
            public double evaluate(EvaluationContext context) {
                return VARIABLE;
            }
            @Override
            public ValidationContext validate(ValidationContext context) {
                if (context.context() == ValidationContext.Context.NODE) {
                    if (!name().equals("n")) {
                        return context.withError(new SemanticErrors.SemanticError(formatWithLocale(
                            "Invalid variable `%s`. Only `n` is allowed for nodes",
                            name()
                        )));
                    }
                } else if (context.context() == ValidationContext.Context.RELATIONSHIP) {
                    if (!name().equals("r")) {
                        return context.withError(new SemanticErrors.SemanticError(formatWithLocale(
                            "Invalid variable `%s`. Only `r` is allowed for relationships",
                            name()
                        )));
                    }
                }

                return context;
            }
            @Override
            public String prettyString() {
                return name;
            }
        }

    }

    interface UnaryExpression extends Expression {

        Expression in();

        @Override
        default ValidationContext validate(ValidationContext context) {
            return in().validate(context);
        }

        interface Property extends UnaryExpression {

            String propertyKey();

            @Override
            default double evaluate(EvaluationContext context) {
                return context.getProperty(propertyKey(), valueType());
            }

            @Override
            default ValidationContext validate(ValidationContext context) {
                context = in().validate(context);

                Set<String> availablePropertyKeys = context.availableProperties().keySet();

                if (!availablePropertyKeys.contains(propertyKey())) {
                    return context.withError(new SemanticErrors.SemanticError(prettySuggestions(
                        formatWithLocale(
                            "Unknown property `%s`.",
                            propertyKey()
                        ),
                        propertyKey(),
                        availablePropertyKeys
                    )));
                }
                var propertyType = context.availableProperties().get(propertyKey());
                if (propertyType != ValueType.LONG && propertyType != ValueType.DOUBLE) {
                    return context.withError(new SemanticErrors.SemanticError(
                        formatWithLocale(
                            "Unsupported property type `%s` for expression `%s`. Supported types %s",
                            propertyType.name(),
                            prettyString(),
                            StringJoining.join(List.of(ValueType.LONG.name(), ValueType.DOUBLE.name()))
                        )));
                }
                return context;
            }

            @Override
            default String prettyString() {
                return in().prettyString() + "." + propertyKey();
            }
        }

        record MyProperty(Expression in, String propertyKey, ValueType valueType) implements Property {
            @Override
            public double evaluate(EvaluationContext context) {
                return context.getProperty(propertyKey, valueType);
            }
            @Override
            public ValidationContext validate(ValidationContext context) {
                context = in().validate(context);
                Set<String> availablePropertyKeys = context.availableProperties().keySet();
                if (!availablePropertyKeys.contains(propertyKey())) {
                    return context.withError(new SemanticErrors.SemanticError(prettySuggestions(
                        formatWithLocale(
                            "Unknown property `%s`.",
                            propertyKey()
                        ),
                        propertyKey(),
                        availablePropertyKeys
                    )));
                }
                var propertyType = context.availableProperties().get(propertyKey());
                if (propertyType != ValueType.LONG && propertyType != ValueType.DOUBLE) {
                    return context.withError(new SemanticErrors.SemanticError(
                        formatWithLocale(
                            "Unsupported property type `%s` for expression `%s`. Supported types %s",
                            propertyType.name(),
                            prettyString(),
                            StringJoining.join(List.of(ValueType.LONG.name(), ValueType.DOUBLE.name()))
                        )));
                }
                return context;
            }
            @Override
            public String prettyString() {
                return in().prettyString() + "." + propertyKey();
            }
        }

        interface HasNodeLabels extends UnaryExpression {
            List<NodeLabel> nodeLabels();

            @Override
            default double evaluate(EvaluationContext context) {
                return context.hasNodeLabels(nodeLabels()) ? TRUE : FALSE;
            }

            @Override
            default ValidationContext validate(ValidationContext context) {
                context = in().validate(context);

                var availableNodeLabels = context.availableNodeLabels();

                for (var nodeLabel : nodeLabels()) {
                    if (!availableNodeLabels.contains(nodeLabel)) {
                        context = trackMissingLabelOrTypeError(
                            context,
                            nodeLabel.name,
                            availableNodeLabels.stream().map(NodeLabel::name).collect(Collectors.toList())
                        );
                    }
                }
                return context;
            }
        }

        record MyHasNodeLabels(Expression in, List<NodeLabel> nodeLabels) implements HasNodeLabels {
            @Override
            public double evaluate(EvaluationContext context) {
                return context.hasNodeLabels(nodeLabels) ? TRUE : FALSE;
            }
            @Override
            public ValidationContext validate(ValidationContext context) {
                context = in().validate(context);
                var availableNodeLabels = context.availableNodeLabels();
                for (var nodeLabel : nodeLabels()) {
                    if (!availableNodeLabels.contains(nodeLabel)) {
                        context = trackMissingLabelOrTypeError(
                            context,
                            nodeLabel.name,
                            availableNodeLabels.stream().map(NodeLabel::name).collect(Collectors.toList())
                        );
                    }
                }
                return context;
            }
        }

        interface HasRelationshipTypes extends UnaryExpression {
            List<RelationshipType> relationshipTypes();

            @Override
            default double evaluate(EvaluationContext context) {
                return context.hasRelationshipTypes(relationshipTypes()) ? TRUE : FALSE;
            }

            @Override
            default ValidationContext validate(ValidationContext context) {
                context = in().validate(context);

                var availableRelationshipTypes = context.availableRelationshipTypes();

                for (var relationshipType : relationshipTypes()) {
                    if (!availableRelationshipTypes.contains(relationshipType)) {
                        context = trackMissingLabelOrTypeError(
                            context,
                            relationshipType.name,
                            availableRelationshipTypes.stream().map(RelationshipType::name).collect(Collectors.toList())
                        );
                    }
                }
                return context;
            }
        }

        record MyHasRelationshipTypes(Expression in, List<RelationshipType> relationshipTypes) implements HasRelationshipTypes {
            @Override
            public double evaluate(EvaluationContext context) {
                return context.hasRelationshipTypes(relationshipTypes) ? TRUE : FALSE;
            }
            @Override
            public ValidationContext validate(ValidationContext context) {
                context = in().validate(context);
                var availableRelationshipTypes = context.availableRelationshipTypes();
                for (var relationshipType : relationshipTypes()) {
                    if (!availableRelationshipTypes.contains(relationshipType)) {
                        context = trackMissingLabelOrTypeError(
                            context,
                            relationshipType.name,
                            availableRelationshipTypes.stream().map(RelationshipType::name).toList()
                        );
                    }
                }
                return context;
            }
        }

        interface Not extends UnaryExpression {

            @Override
            default double evaluate(EvaluationContext context) {
                return in().evaluate(context) == TRUE ? FALSE : TRUE;
            }

        }

        record MyNot(Expression in) implements Not {
            @Override
            public double evaluate(EvaluationContext context) {
                return in().evaluate(context) == FALSE ? TRUE : FALSE;
            }
        }

        interface NewParameter extends UnaryExpression {

            @Override
            LeafExpression.Variable in();

            @Override
            default ValueType valueType() {
                return ValueType.UNKNOWN;
            }

            @Override
            default double evaluate(EvaluationContext context) {
                var resolvedParameter = context.resolveParameter(in().name());
                if (resolvedParameter instanceof Long) {
                    return resolvedParameter.longValue();
                }
                return resolvedParameter.doubleValue();
            }

            @Override
            default ValidationContext validate(ValidationContext context) {
                return context;
            }
        }

        record MyNewParameter(LeafExpression.Variable in) implements NewParameter {
            @Override
            public ValueType valueType() {
                return ValueType.UNKNOWN;
            }
            @Override
            public double evaluate(EvaluationContext context) {
                var resolvedParameter = context.resolveParameter(in().name());
                if (resolvedParameter instanceof Long) {
                    return resolvedParameter.longValue();
                }
                return resolvedParameter.doubleValue();
            }
            @Override
            public ValidationContext validate(ValidationContext context) {
                return context;
            }
        }
    }

    interface BinaryExpression extends Expression {

        Expression lhs();

        Expression rhs();

        @Override
        default ValidationContext validate(ValidationContext context) {
            context = lhs().validate(context);
            return rhs().validate(context);
        }

        interface And extends BinaryExpression {
            @Override
            default double evaluate(EvaluationContext context) {
                return lhs().evaluate(context) == TRUE && rhs().evaluate(context) == TRUE
                    ? TRUE
                    : FALSE;
            }
        }

        interface Or extends BinaryExpression {

            @Override
            default double evaluate(EvaluationContext context) {
                return lhs().evaluate(context) == TRUE || rhs().evaluate(context) == TRUE
                    ? TRUE
                    : FALSE;
            }
        }

        interface Xor extends BinaryExpression {

            @Override
            default double evaluate(EvaluationContext context) {
                return lhs().evaluate(context) == TRUE ^ rhs().evaluate(context) == TRUE
                    ? TRUE
                    : FALSE;
            }
        }

        record MyAnd(Expression lhs, Expression rhs) implements And {
            @Override
            public double evaluate(EvaluationContext context) {
                return lhs.evaluate(context) == TRUE && rhs().evaluate(context) == TRUE
                    ? TRUE
                    : FALSE;
            }
        }

        record MyOr(Expression lhs, Expression rhs) implements Or {
            @Override
            public double evaluate(EvaluationContext context) {
                return lhs().evaluate(context) == TRUE || rhs().evaluate(context) == TRUE
                    ? TRUE
                    : FALSE;
            }
        }

        record MyXor(Expression lhs, Expression rhs) implements Xor {
            @Override
            public double evaluate(EvaluationContext context) {
                return lhs().evaluate(context) == TRUE ^ rhs().evaluate(context) == TRUE
                    ? TRUE
                    : FALSE;
            }
        }

        interface BinaryArithmeticExpression extends BinaryExpression {

            @Override
            default double evaluate(EvaluationContext context) {
                var lhsValue = lhs().evaluate(context);
                var rhsValue = rhs().evaluate(context);

                // It is sufficient to check one of the input types
                // as validation made sure that the types are equal.
                if (lhs().valueType() == ValueType.LONG) {
                    long convertedRhsValue = rhs().valueType() == ValueType.UNKNOWN
                        ? (long) rhsValue
                        : Double.doubleToRawLongBits(rhsValue);
                    return evaluateLong(Double.doubleToRawLongBits(lhsValue), convertedRhsValue);
                }

                return evaluateDouble(lhsValue, rhsValue);

            }

            double evaluateLong(long lhsValue, long rhsValue);

            double evaluateDouble(double lhsValue, double rhsValue);

            @Override
            default ValidationContext validate(ValidationContext context) {
                context = lhs().validate(context);
                context = rhs().validate(context);

                var leftType = lhs().valueType();
                var rightType = rhs().valueType();

                // If one of the types is UNKNOWN, the corresponding property does not exist
                // in the graph store, and we already reported this as an error when parsing
                // the property expression. There is no need to add additional info.
                if (leftType != rightType && leftType != ValueType.UNKNOWN && rightType != ValueType.UNKNOWN) {
                    var changeProposal = literalTypeHint(lhs(), rhs())
                        .map(s -> formatWithLocale(" Try changing the literal to `%s`.", s))
                        .orElse("");

                    return context.withError(new SemanticErrors.SemanticError(
                        formatWithLocale(
                            "Incompatible types `%s` and `%s` in binary expression `%s`.%s",
                            leftType,
                            rightType,
                            prettyString(),
                            changeProposal
                        )));
                }

                return context;
            }
        }

        interface Equal extends BinaryArithmeticExpression {

            @Override
            default double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue == rhsValue ? TRUE : FALSE;
            }

            @Override
            default double evaluateDouble(double lhsValue, double rhsValue) {
                return Math.abs(lhsValue - rhsValue) < EPSILON ? TRUE : FALSE;
            }

            @Override
            default String prettyString() {
                return lhs().prettyString() + " = " + rhs().prettyString();
            }
        }

        interface NotEqual extends BinaryArithmeticExpression {

            @Override
            default double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue != rhsValue ? TRUE : FALSE;
            }

            @Override
            default double evaluateDouble(double lhsValue, double rhsValue) {
                return Math.abs(lhsValue - rhsValue) > EPSILON ? TRUE : FALSE;
            }

            @Override
            default String prettyString() {
                return lhs().prettyString() + " <> " + rhs().prettyString();
            }
        }

        interface GreaterThan extends BinaryArithmeticExpression {

            @Override
            default double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue > rhsValue ? TRUE : FALSE;
            }

            @Override
            default double evaluateDouble(double lhsValue, double rhsValue) {
                return (lhsValue - rhsValue) > EPSILON ? TRUE : FALSE;
            }

            @Override
            default String prettyString() {
                return lhs().prettyString() + " > " + rhs().prettyString();
            }
        }

        record MyEqual(Expression lhs, Expression rhs) implements BinaryExpression.Equal {
            public double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue == rhsValue ? TRUE : FALSE;
            }
            public double evaluateDouble(double lhsValue, double rhsValue) {
                return Math.abs(lhsValue - rhsValue) < EPSILON ? TRUE : FALSE;
            }
            public String prettyString() {
                return lhs.prettyString() + " = " + rhs.prettyString();
            }
        }

        record MyNotEqual(Expression lhs, Expression rhs) implements BinaryExpression.NotEqual {
            public double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue != rhsValue ? TRUE : FALSE;
            }
            public double evaluateDouble(double lhsValue, double rhsValue) {
                return Math.abs(lhsValue - rhsValue) > EPSILON ? TRUE : FALSE;
            }
            public String prettyString() {
                return lhs.prettyString() + " <> " + rhs.prettyString();
            }
        }

        record MyGreaterThan(Expression lhs, Expression rhs) implements BinaryExpression.GreaterThan {
            public double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue > rhsValue ? TRUE : FALSE;
            }
            public double evaluateDouble(double lhsValue, double rhsValue) {
                return (lhsValue - rhsValue) > EPSILON ? TRUE : FALSE;
            }
            public String prettyString() {
                return lhs.prettyString() + " > " + rhs.prettyString();
            }
        }

        interface GreaterThanOrEquals extends BinaryArithmeticExpression {

            @Override
            default double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue >= rhsValue ? TRUE : FALSE;
            }

            @Override
            default double evaluateDouble(double lhsValue, double rhsValue) {
                return lhsValue > rhsValue || Math.abs(lhsValue - rhsValue) < EPSILON ? TRUE : FALSE;
            }

            @Override
            default String prettyString() {
                return lhs().prettyString() + " >= " + rhs().prettyString();
            }
        }

        interface LessThan extends BinaryArithmeticExpression {

            @Override
            default double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue < rhsValue ? TRUE : FALSE;
            }

            @Override
            default double evaluateDouble(double lhsValue, double rhsValue) {
                return (rhsValue - lhsValue) > EPSILON ? TRUE : FALSE;
            }

            @Override
            default String prettyString() {
                return lhs().prettyString() + " < " + rhs().prettyString();
            }
        }

        interface LessThanOrEquals extends BinaryArithmeticExpression {

            @Override
            default double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue <= rhsValue ? TRUE : FALSE;
            }

            @Override
            default double evaluateDouble(double lhsValue, double rhsValue) {
                return lhsValue < rhsValue || (rhsValue - lhsValue) > -EPSILON ? TRUE : FALSE;
            }

            @Override
            default String prettyString() {
                return lhs().prettyString() + " <= " + rhs().prettyString();
            }
        }

        record MyGreaterThanOrEquals(Expression lhs, Expression rhs) implements BinaryExpression.GreaterThanOrEquals {
            @Override
            public double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue >= rhsValue ? TRUE : FALSE;
            }
            @Override
            public double evaluateDouble(double lhsValue, double rhsValue) {
                return lhsValue > rhsValue || Math.abs(lhsValue - rhsValue) < EPSILON ? TRUE : FALSE;
            }
            @Override
            public String prettyString() {
                return lhs().prettyString() + " >= " + rhs().prettyString();
            }
        }

        record MyLessThan(Expression lhs, Expression rhs) implements BinaryExpression.LessThan {
            @Override
            public double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue < rhsValue ? TRUE : FALSE;
            }
            @Override
            public double evaluateDouble(double lhsValue, double rhsValue) {
                return (rhsValue - lhsValue) > EPSILON ? TRUE : FALSE;
            }
            @Override
            public String prettyString() {
                return lhs.prettyString() + " < " + rhs.prettyString();
            }
        }

        record MyLessThanOrEquals(Expression lhs, Expression rhs) implements BinaryExpression.LessThanOrEquals {
            @Override
            public double evaluateLong(long lhsValue, long rhsValue) {
                return lhsValue <= rhsValue ? TRUE : FALSE;
            }
            @Override
            public double evaluateDouble(double lhsValue, double rhsValue) {
                return lhsValue < rhsValue || (rhsValue - lhsValue) > -EPSILON ? TRUE : FALSE;
            }
            @Override
            public String prettyString() {
                return lhs().prettyString() + " <= " + rhs().prettyString();
            }
        }
    }

    interface Literal extends Expression {
        interface LongLiteral extends Literal {
            long value();

            @Override
            default ValueType valueType() {
                return ValueType.LONG;
            }

            @Override
            default double evaluate(EvaluationContext context) {
                return Double.longBitsToDouble(value());
            }

            @Override
            default String prettyString() {return Long.toString(value());}
        }

        record MyLongLiteral(long value) implements LongLiteral {
            @Override
            public ValueType valueType() {
                return ValueType.LONG;
            }
            @Override
            public double evaluate(EvaluationContext context) {
                return Double.longBitsToDouble(value);
            }
            @Override
            public String prettyString() {
                return Long.toString(value);
            }
        }

        interface DoubleLiteral extends Literal {
            double value();

            @Override
            default ValueType valueType() {
                return ValueType.DOUBLE;
            }

            @Override
            default double evaluate(EvaluationContext context) {
                return value();
            }

            default String prettyString() {return Double.toString(value());}

        }

        record MyDoubleLiteral(double value) implements DoubleLiteral {
            @Override
            public ValueType valueType() {
                return ValueType.DOUBLE;
            }
            @Override
            public double evaluate(EvaluationContext context) {
                return value;
            }
            @Override
            public String prettyString() {
                return Double.toString(value);
            }
        }

        interface TrueLiteral extends Literal {

            TrueLiteral INSTANCE = new MyTrueLiteral();

            @Override
            default double evaluate(EvaluationContext context) {
                return TRUE;
            }
        }

        record MyTrueLiteral() implements TrueLiteral {
            @Override
            public double evaluate(EvaluationContext context) {
                return TRUE;
            }
        }

        interface FalseLiteral extends Literal {

            FalseLiteral INSTANCE = new MyFalseLiteral();

            @Override
            default double evaluate(EvaluationContext context) {
                return FALSE;
            }
        }

        record MyFalseLiteral() implements FalseLiteral {
            @Override
            public double evaluate(EvaluationContext context) {
                return FALSE;
            }
        }

        interface StringLiteral extends Literal {
            String value();

            @Override
            default ValueType valueType() {
                return ValueType.DOUBLE;
            }

            @Override
            default double evaluate(EvaluationContext context) {
                return value().hashCode();
            }

            default String prettyString() {return value();}
        }

        record MyStringLiteral(String value) implements StringLiteral {
            @Override
            public ValueType valueType() {
                return ValueType.DOUBLE;
            }
            @Override
            public double evaluate(EvaluationContext context) {
                return value().hashCode();
            }
            @Override
            public String prettyString() {
                return value;
            }
        }
    }

    static Optional<String> literalTypeHint(Expression lhs, Expression rhs) {
        var lhsIsLiteral = lhs instanceof Literal;
        var rhsIsLiteral = rhs instanceof Literal;
        if (lhsIsLiteral && rhsIsLiteral || !lhsIsLiteral && !rhsIsLiteral) {
            return Optional.empty();
        }

        var lit = lhs instanceof Literal ? lhs : rhs;
        var nonLit = lhs instanceof Literal ? rhs : lhs;

        var proposedLiteral = Optional.<String>empty();

        if (lit.valueType() == ValueType.DOUBLE && nonLit.valueType() == ValueType.LONG) {
            var doubleLiteral = (Literal.DoubleLiteral) lit;
            proposedLiteral = Optional.of(Long.toString((long) doubleLiteral.value()));
        }
        if (lit.valueType() == ValueType.LONG && nonLit.valueType() == ValueType.DOUBLE) {
            var longLiteral = (Literal.LongLiteral) lit;
            proposedLiteral = Optional.of(Double.toString((double) longLiteral.value()));
        }

        return proposedLiteral;
    }

    static ValidationContext trackMissingLabelOrTypeError(
        ValidationContext context,
        String labelOrType,
        Collection<String> availableLabelsOrTypes
    ) {
        var elementType = context.context() == ValidationContext.Context.NODE
            ? "label"
            : "relationship type";

        return context.withError(new SemanticErrors.SemanticError(prettySuggestions(
            formatWithLocale(
                "Unknown %s `%s`.",
                elementType,
                labelOrType
            ),
            labelOrType,
            availableLabelsOrTypes
        )));
    }

    interface Function extends Expression {

        interface Degree extends Function {

            String NAME = "degree";

            Set<RelationshipType> typeSelection();

            @Override
            default ValueType valueType() {
                return ValueType.LONG;
            }

            @Override
            default double evaluate(EvaluationContext context) {
                long degree = context.degree(this.typeSelection());
                return Double.longBitsToDouble(degree);
            }
        }

        record MyDegree(Set<RelationshipType> typeSelection) implements Degree {
            @Override
            public ValueType valueType() {
                return ValueType.LONG;
            }
            @Override
            public double evaluate(EvaluationContext context) {
                long degree = context.degree(this.typeSelection);
                return Double.longBitsToDouble(degree);
            }
        }
    }

}
