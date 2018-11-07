package com.github.ellie.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.IGNORED;
import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExploratoryRunnerShould {

    private static final BiConsumer<String, TestResult> IGNORE_PASSING_CASES_CONSUMER = (l, o) -> {
    };
    private static final TestResult PASSING_RESULTS = new TestResult(Map.of(PASS, List.of(ExplorationArguments.of(1))));
    private static final TestResult FAILING_RESULTS = new TestResult(Map.of(FAIL, List.of(ExplorationArguments.of(2))));
    private static final TestResult MIXTED_RESULTS = new TestResult(Map.of(PASS, List.of(ExplorationArguments.of(1)),
            FAIL, List.of(ExplorationArguments.of(2)),
            IGNORED, List.of(ExplorationArguments.of(3))
    ));

    private ExplorationResults results;
    private Runner exploratoryRunner;

    @BeforeEach
    void createRunner() {
        results = mock(ExplorationResults.class);
        exploratoryRunner = new ExploratoryRunner(IGNORE_PASSING_CASES_CONSUMER);
    }

    private static Stream<Arguments> passingResults() {
        return Stream.of(
                Arguments.of(Map.of("passing", PASSING_RESULTS)),
                Arguments.of(Map.of("passing", PASSING_RESULTS,
                        "passing again", PASSING_RESULTS))
        );
    }

    private static Stream<Arguments> failingResults() {
        return Stream.of(
                Arguments.of(Map.of("failing", FAILING_RESULTS)),
                Arguments.of(Map.of("failing", FAILING_RESULTS,
                        "failing again", FAILING_RESULTS))
        );
    }

    private static Stream<Arguments> mixtedResults() {
        return Stream.of(
                Arguments.of(Map.of("mixted", MIXTED_RESULTS)),
                Arguments.of(Map.of("mixted", MIXTED_RESULTS,
                        "mixted again", MIXTED_RESULTS))
        );
    }

    @ParameterizedTest
    @MethodSource({"passingResults", "failingResults", "mixtedResults"})
    void createsOneTestPerPostCondition(Map<String, TestResult> results) {
        when(this.results.resultByBehaviour()).thenReturn(results);

        Stream<ConditionTest> behaviours = exploratoryRunner.tests(this.results);

        assertThat(behaviours).hasSize(results.size())
                .extracting(c -> c.name)
                .containsAll(results.keySet());
    }


    @ParameterizedTest
    @MethodSource({"passingResults", "mixtedResults", "failingResults"})
    void callsConsumerWithTestResultAfterRun(Map<String, TestResult> results) {
        Map<String, TestResult> testResults = new HashMap<>();
        when(this.results.resultByBehaviour()).thenReturn(results);

        Stream<ConditionTest> behaviours = new ExploratoryRunner(testResults::put).tests(this.results);

        behaviours.forEach(t -> {
            try {
                t.test.run();
            } catch (AssertionError ignored) {
            }
        });

        assertThat(testResults).isEqualTo(results);
    }


    @ParameterizedTest
    @MethodSource({"passingResults", "mixtedResults"})
    void passIfALeastOneDataPasses(Map<String, TestResult> results) {
        when(this.results.resultByBehaviour()).thenReturn(results);
        Stream<ConditionTest> behaviours = exploratoryRunner.tests(this.results);
        try {
            behaviours.forEach(t -> t.test.run());
        } catch (AssertionError e) {
            fail("No exception should be thrown but got : " + e);
        }
    }

    @ParameterizedTest
    @MethodSource("failingResults")
    void failPotentialBehaviourIfNotDataValidatesPredicate(Map<String, TestResult> results) {
        when(this.results.resultByBehaviour()).thenReturn(results);
        Stream<ConditionTest> behaviours = exploratoryRunner.tests(this.results);

        Assertions.assertThatThrownBy(() -> behaviours.forEach(t -> t.test.run()))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("no data validates this behaviour");
    }

}
