package com.github.ellie.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.*;
import static com.github.ellie.core.Explorer.explore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class ExplorerShould {

    static Stream<Arguments> methodNames() {
        return Stream.of(
                Arguments.of(List.of(conditionThatPasses("test"))),
                Arguments.of(List.of(conditionThatPasses("firstMethod"), conditionThatPasses("secondMethod")))
        );
    }

    @ParameterizedTest
    @MethodSource("methodNames")
    void nameNodesWithActionAndSupposedBehaviour(List<ExplorableCondition> conditions) {
        PostConditionResults results = explore(List.of(ExplorationArguments.of(2)),
                new PostConditions(conditions));

        List<String> names = conditions.stream().map(explorableCondition -> explorableCondition.name().value).collect(Collectors.toList());
        assertThat(results.resultByPostConditions().keySet())
                .extracting(e->e.value)
                .containsAll(names);
    }

    @Test
    void groupResultsByConditions() {
        ExplorationArguments two = ExplorationArguments.of(2);
        ExplorationArguments four = ExplorationArguments.of(4);
        ExplorationArguments ignore = ExplorationArguments.of(-42);
        PostConditionResults results = explore(List.of(two, four, ignore),
                new PostConditions(List.of(condition("argIs2", i -> i == two ? PASS : i == ignore ? IGNORED : FAIL),
                        condition("argIs4", i -> i == four ? PASS : i == ignore ? IGNORED : FAIL))));
        Map<Name, TestResult> resultByBehaviour = results.resultByPostConditions();

        TestResult argIs2 = resultByBehaviour.get(new Name("argIs2"));
        assertThat(argIs2.passingData()).containsOnly(two);
        assertThat(argIs2.failingData()).containsOnly(four);
        assertThat(argIs2.ignoredData()).containsOnly(ignore);

        TestResult argIs4 = resultByBehaviour.get(new Name("argIs4"));
        assertThat(argIs4.passingData()).containsOnly(four);
        assertThat(argIs4.failingData()).containsOnly(two);
        assertThat(argIs4.ignoredData()).containsOnly(ignore);

    }

    @Test
    void testEachBehavioursWithEachData() {
        ExplorationArguments firstInput = ExplorationArguments.of(2);
        ExplorationArguments secondInput = ExplorationArguments.of(4);
        ExplorableCondition passes = Mockito.spy(conditionThatPasses("test_passes"));
        ExplorableCondition fails = Mockito.spy(conditionThatFails("test_fails"));
        explore(List.of(firstInput, secondInput), new PostConditions(List.of(passes, fails)));

        verify(passes).testWith(firstInput);
        verify(passes).testWith(secondInput);
        verify(fails).testWith(firstInput);
        verify(fails).testWith(secondInput);
    }

    private static ExplorableCondition condition(String name, Function<ExplorationArguments, ConditionOutput> predicate) {
        return new ExplorableCondition() {
            @Override
            public ConditionOutput testWith(ExplorationArguments explorationArguments) {
                return predicate.apply(explorationArguments);
            }

            @Override
            public Name name() {
                return new Name(name);
            }
        };
    }

    private static ExplorableCondition conditionThatFails(String name) {
        return condition(name, i -> FAIL);
    }
    private static ExplorableCondition conditionThatPasses(String name) {
        return condition(name, i -> PASS);
    }

}
