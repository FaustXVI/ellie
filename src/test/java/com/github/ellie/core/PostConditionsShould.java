package com.github.ellie.core;

import com.github.ellie.core.conditions.PostConditions;
import com.github.ellie.core.explorers.Explorer;
import com.github.ellie.core.explorers.TestResult;
import com.github.ellie.core.conditions.NamedCondition;
import com.github.ellie.core.conditions.NamedConditionResult;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class PostConditionsShould {

    static Stream<Arguments> methodNames() {
        return Stream.of(
                Arguments.of(List.of(conditionThatPasses("test"))),
                Arguments.of(List.of(conditionThatPasses("firstMethod"), conditionThatPasses("secondMethod")))
        );
    }

    @ParameterizedTest
    @MethodSource("methodNames")
    void nameNodesWithActionAndSupposedBehaviour(List<NamedCondition> conditions) {
        Explorer.PostConditionResults results = new PostConditions(conditions).explore(List.of(ExplorationArguments.of(2)));

        List<String> names = conditions.stream().map(condition -> condition.name().value).collect(Collectors.toList());
        assertThat(results.resultByName().keySet())
                .extracting(e->e.value)
                .containsAll(names);
    }

    @Test
    void groupResultsByConditions() {
        ExplorationArguments two = ExplorationArguments.of(2);
        ExplorationArguments four = ExplorationArguments.of(4);
        Explorer.PostConditionResults results = new PostConditions(List.of(condition("argIs2", i -> i == two ? PASS : FAIL),
                condition("argIs4", i -> i == four ? PASS  : FAIL))).explore(List.of(two, four));
        Map<Name, TestResult> resultByBehaviour = results.resultByName();

        TestResult argIs2 = resultByBehaviour.get(new Name("argIs2"));
        assertThat(argIs2.argumentsThat(PASS)).containsOnly(two);
        assertThat(argIs2.argumentsThat(FAIL)).containsOnly(four);

        TestResult argIs4 = resultByBehaviour.get(new Name("argIs4"));
        assertThat(argIs4.argumentsThat(PASS)).containsOnly(four);
        assertThat(argIs4.argumentsThat(FAIL)).containsOnly(two);

    }

    @Test
    void testEachBehavioursWithEachData() {
        ExplorationArguments firstInput = ExplorationArguments.of(2);
        ExplorationArguments secondInput = ExplorationArguments.of(4);
        NamedCondition passes = Mockito.spy(conditionThatPasses("test_passes"));
        NamedCondition fails = Mockito.spy(conditionThatFails("test_fails"));
        new PostConditions(List.of(passes, fails)).explore(List.of(firstInput, secondInput));

        verify(passes).testWith(firstInput);
        verify(passes).testWith(secondInput);
        verify(fails).testWith(firstInput);
        verify(fails).testWith(secondInput);
    }

    private static NamedCondition condition(String name, Function<ExplorationArguments, ConditionOutput> predicate) {
        return new NamedCondition() {
            @Override
            public NamedConditionResult testWith(ExplorationArguments explorationArguments) {
                return new NamedConditionResult(name(),predicate.apply(explorationArguments),explorationArguments);
            }

            @Override
            public Name name() {
                return new Name(name);
            }
        };
    }

    private static NamedCondition conditionThatFails(String name) {
        return condition(name, i -> FAIL);
    }
    private static NamedCondition conditionThatPasses(String name) {
        return condition(name, i -> PASS);
    }

}
