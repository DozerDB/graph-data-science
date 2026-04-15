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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.And;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.Equal;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.GreaterThan;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.GreaterThanOrEquals;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.LessThan;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.LessThanOrEquals;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.NotEqual;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.Or;
import org.neo4j.gds.beta.filter.expression.Expression.BinaryExpression.Xor;
import org.neo4j.gds.beta.filter.expression.Expression.Function.Degree;
import org.neo4j.gds.beta.filter.expression.Expression.LeafExpression.Variable;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.DoubleLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.FalseLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.LongLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.StringLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.Literal.TrueLiteral;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.Not;
import org.neo4j.gds.beta.filter.expression.Expression.UnaryExpression.Property;
import org.neo4j.gds.utils.StringJoining;
import org.opencypher.v9_0.parser.javacc.ParseException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.neo4j.gds.utils.StringFormatting.formatWithLocale;

class ExpressionParserTest {

    // unary

    static Stream<Arguments> nots() {
        return Stream.of(
            Arguments.of("NOT TRUE", new Not(TrueLiteral.INSTANCE)),
            Arguments.of("NOT FALSE", new Not(FalseLiteral.INSTANCE)),
            Arguments.of(
                "NOT (TRUE OR FALSE)",
                new Not(
                    new Or(TrueLiteral.INSTANCE, FalseLiteral.INSTANCE)
                )
            )
        );
    }


    @ParameterizedTest
    @MethodSource("nots")
    void nots(String cypher, Not expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    // literal

    @Test
    void trueLiteral() throws ParseException {
        var actual = ExpressionParser.parse("TRUE", Map.of());
        assertThat(actual).isEqualTo(TrueLiteral.INSTANCE);
    }

    @Test
    void falseLiteral() throws ParseException {
        var actual = ExpressionParser.parse("FALSE", Map.of());
        assertThat(actual).isEqualTo(FalseLiteral.INSTANCE);
    }

    static Stream<Arguments> longs() {
        return Stream.of(
            Arguments.of("42", new LongLiteral(42)),
            Arguments.of("-42", new LongLiteral(-42)),
            Arguments.of("0", new LongLiteral(0)),
            Arguments.of("1337", new LongLiteral(1337))
        );
    }

    @ParameterizedTest
    @MethodSource("longs")
    void longLiteral(String cypher, LongLiteral expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> doubles() {
        return Stream.of(
            Arguments.of("42.0", new DoubleLiteral(42.0)),
            Arguments.of("-42.0", new DoubleLiteral(-42.0)),
            Arguments.of("0.0", new DoubleLiteral(0.0)),
            Arguments.of("13.37", new DoubleLiteral(13.37)),
            Arguments.of("-13.37", new DoubleLiteral(-13.37))
        );
    }

    @ParameterizedTest
    @MethodSource("doubles")
    void doubleLiteral(String cypher, DoubleLiteral expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Foo", "bar", "BAZ", "42"})
    void stringLiteral(String string) throws ParseException {
        var actual = ExpressionParser.parse("'" + string + "'", Map.of());
        assertThat(actual).isEqualTo(new StringLiteral(string));
    }

    // binary

    static Stream<Arguments> ands() {
        return Stream.of(
            Arguments.of(
                "TRUE AND FALSE",
                new And(TrueLiteral.INSTANCE, FalseLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE AND TRUE",
                new And(TrueLiteral.INSTANCE, TrueLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE AND TRUE AND FALSE",
                new And(
                    new And(
                        TrueLiteral.INSTANCE,
                        TrueLiteral.INSTANCE
                    ), FalseLiteral.INSTANCE
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("ands")
    void and(String cypher, And expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> ors() {
        return Stream.of(
            Arguments.of(
                "TRUE OR FALSE",
                new Or(TrueLiteral.INSTANCE, FalseLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE OR TRUE",
                new Or(TrueLiteral.INSTANCE, TrueLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE OR TRUE OR FALSE",
                new Or(
                    new Or(
                        TrueLiteral.INSTANCE,
                        TrueLiteral.INSTANCE
                    ), FalseLiteral.INSTANCE
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("ors")
    void or(String cypher, Or expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> xors() {
        return Stream.of(
            Arguments.of(
                "TRUE XOR FALSE",
                new Xor(TrueLiteral.INSTANCE, FalseLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE XOR TRUE",
                new Xor(TrueLiteral.INSTANCE, TrueLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE XOR TRUE XOR FALSE",
                new Xor(
                    new Xor(
                        TrueLiteral.INSTANCE,
                        TrueLiteral.INSTANCE
                    ), FalseLiteral.INSTANCE
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("xors")
    void xor(String cypher, Xor expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> equals() {
        return Stream.of(
            Arguments.of(
                "TRUE = FALSE",
                new Equal(TrueLiteral.INSTANCE, FalseLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE = TRUE",
                new Equal(TrueLiteral.INSTANCE, TrueLiteral.INSTANCE)
            ),
            Arguments.of(
                "TRUE = (TRUE = FALSE)",
                new Equal(
                    TrueLiteral.INSTANCE,
                    new Equal(
                        TrueLiteral.INSTANCE,
                        FalseLiteral.INSTANCE
                    )
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("equals")
    void equal(String cypher, Equal expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> notEquals() {
        return Stream.of(
            Arguments.of(
                "TRUE <> FALSE",
                new NotEqual(
                    TrueLiteral.INSTANCE,
                    FalseLiteral.INSTANCE
                )
            ),
            Arguments.of(
                "TRUE != FALSE",
                new NotEqual(
                    TrueLiteral.INSTANCE,
                    FalseLiteral.INSTANCE
                )
            ),
            Arguments.of(
                "TRUE <> TRUE",
                new NotEqual(
                    TrueLiteral.INSTANCE,
                    TrueLiteral.INSTANCE
                )
            ),
            Arguments.of(
                "TRUE <> (TRUE <> FALSE)",
                new NotEqual(
                    TrueLiteral.INSTANCE,
                    new NotEqual(
                        TrueLiteral.INSTANCE,
                        FalseLiteral.INSTANCE
                    )
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("notEquals")
    void notEqual(String cypher, NotEqual expected) throws ParseException {
        var actual = ExpressionParser.parse(cypher, Map.of());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void greaterThan() throws ParseException {
        var actual = ExpressionParser.parse("1337 > 42", Map.of());
        assertThat(actual).isEqualTo(new GreaterThan(
            new LongLiteral(1337),
            new LongLiteral(42))
        );
    }

    @Test
    void greaterThanEquals() throws ParseException {
        var actual = ExpressionParser.parse("1337 >= 42", Map.of());
        assertThat(actual).isEqualTo(new GreaterThanOrEquals(
            new LongLiteral(1337),
            new LongLiteral(42))
        );
    }

    @Test
    void lessThan() throws ParseException {
        var actual = ExpressionParser.parse("1337 < 42", Map.of());

        assertThat(actual).isEqualTo(new LessThan(
            new LongLiteral(1337),
            new LongLiteral(42)
        ));
    }

    @Test
    void lessThanEquals() throws ParseException {
        var actual = ExpressionParser.parse("1337 <= 42", Map.of());
        assertThat(actual).isEqualTo(new LessThanOrEquals(
            new LongLiteral(1337),
            new LongLiteral(42)
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "foo AND TRUE bar",
        "TRUE FALSE",
        "TRUE, FALSE",
    })
    void shouldThrowOnMultipleExpressions(String input) {
        assertThatThrownBy(() -> ExpressionParser
            .parse(input, Map.of()))
            .isInstanceOf(ParseException.class)
            .hasMessageContaining(formatWithLocale("Expected a single filter expression, got '%s'", input));
    }

    static Stream<Arguments> properties() {
        return Stream.of(
            Arguments.of("n.foo", Map.of("foo", ValueType.LONG), ValueType.LONG),
            Arguments.of("n.foo", Map.of("foo", ValueType.DOUBLE), ValueType.DOUBLE),
            Arguments.of("n.foo", Map.of(), ValueType.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("properties")
    void property(String exprString, Map<String, ValueType> properties, ValueType expectedValueType) throws ParseException {
        var expr = ExpressionParser.parse(exprString, properties);

        assertThat(expr).isEqualTo(new Property(
            new Variable("n"),
            "foo",
            expectedValueType
        ));
    }

    static Stream<List<String>> types() {
        return Stream.of(
            List.of(),
            List.of("Foo"),
            List.of("Foo", "Bar")
        );
    }

    @ParameterizedTest
    @MethodSource("types")
    void degree(Collection<String> types) throws ParseException {
        var exprString = StringJoining.join(types.stream().map(type -> "'" + type + "'"), ", ", "degree(", ")");
        var expr = ExpressionParser.parse(exprString, Map.of());
        assertThat(expr).isEqualTo(new Degree(
            types.stream().map(RelationshipType::of).collect(Collectors.toSet())
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"degree", "DEGREE", "dEgReE"})
    void degreeIsCaseInsensitive(String funcName) throws ParseException {
        var exprString = funcName + "()";
        var expr = ExpressionParser.parse(exprString, Map.of());
        assertThat(expr).isEqualTo(new Degree(Set.of()));
    }

    @Test
    void degreeFailsForNonString() {
        assertThatThrownBy(() -> ExpressionParser.parse("degree(42)", Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid argument for `degree`. Only strings are allowed. Got `42`.");
    }

    @Test
    void functionInvocationFailsForUnknownFunction() {
        assertThatThrownBy(() -> ExpressionParser.parse("foobar()", Map.of()))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Unknown function `foobar`.");
    }
}
