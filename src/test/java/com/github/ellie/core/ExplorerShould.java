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
                Arguments.of(List.of("test")),
                Arguments.of(List.of("firstMethod", "secondMethod"))
        );
    }

    @ParameterizedTest
    @MethodSource("methodNames")
    void nameNodesWithActionAndSupposedBehaviour(List<String> names) {
        ExplorationResults results = explore(List.of(ExplorationArguments.of(2)),
                names.stream().map(this::conditionThatPasses).collect(Collectors.toList()));

        assertThat(results.resultByPostConditions().keySet())
                .containsAll(names);
    }

    @Test
    void groupResultsByConditions() {
        ExplorationArguments two = ExplorationArguments.of(2);
        ExplorationArguments four = ExplorationArguments.of(4);
        ExplorationArguments ignore = ExplorationArguments.of(-42);
        ExplorationResults results = explore(List.of(two, four, ignore),
                List.of(condition("argIs2", i -> i == two ? PASS : i == ignore ? IGNORED : FAIL),
                        condition("argIs4", i -> i == four ? PASS : i == ignore ? IGNORED : FAIL)));
        Map<String, TestResult> resultByBehaviour = results.resultByPostConditions();

        TestResult argIs2 = resultByBehaviour.get("argIs2");
        assertThat(argIs2.passingData()).containsOnly(two);
        assertThat(argIs2.failingData()).containsOnly(four);
        assertThat(argIs2.ignoredData()).containsOnly(ignore);

        TestResult argIs4 = resultByBehaviour.get("argIs4");
        assertThat(argIs4.passingData()).containsOnly(four);
        assertThat(argIs4.failingData()).containsOnly(two);
        assertThat(argIs4.ignoredData()).containsOnly(ignore);

    }

    @Test
    void testEachBehavioursWithEachData() {
        ExplorationArguments firstInput = ExplorationArguments.of(2);
        ExplorationArguments secondInput = ExplorationArguments.of(4);
        ExecutableCondition passes = Mockito.spy(conditionThatPasses("test_passes"));
        ExecutableCondition fails = Mockito.spy(conditionThatFails("test_fails"));
        explore(List.of(firstInput, secondInput), List.of(passes, fails));

        verify(passes).testWith(firstInput);
        verify(passes).testWith(secondInput);
        verify(fails).testWith(firstInput);
        verify(fails).testWith(secondInput);
    }

    private ExecutableCondition condition(String name, Function<ExplorationArguments, ConditionOutput> predicate) {
        return new ExecutableCondition() {
            @Override
            public ConditionOutput testWith(ExplorationArguments explorationArguments) {
                return predicate.apply(explorationArguments);
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    private ExecutableCondition conditionThatPasses(String name) {
        return condition(name, i -> PASS);
    }

    private ExecutableCondition conditionThatFails(String name) {
        return condition(name, i -> FAIL);
    }

}
