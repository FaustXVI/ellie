package com.github.ellie.core;

import com.github.ellie.core.asserters.ExploratoryTester;
import com.github.ellie.core.asserters.Tester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExploratoryTesterShould {

    private static final TestResult PASSING_RESULTS = new TestResult(Map.of(PASS, List.of(ExplorationArguments.of(1))));
    private static final TestResult FAILING_RESULTS = new TestResult(Map.of(FAIL, List.of(ExplorationArguments.of(2))));
    private static final TestResult MIXTED_RESULTS = new TestResult(Map.of(
            PASS, List.of(ExplorationArguments.of(1)),
            FAIL, List.of(ExplorationArguments.of(2)),
            IGNORED, List.of(ExplorationArguments.of(3))
    ));
    private static final Consumer<ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };

    private PostConditionResults postConditionResults;
    private Tester exploratoryTester;

    @BeforeEach
    void createRunner() {
        postConditionResults = mock(PostConditionResults.class);
        exploratoryTester = new ExploratoryTester();
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
        when(postConditionResults.resultByPostConditions()).thenReturn(wrapName(results));

        Stream<Exploration> behaviours =
                exploratoryTester.tests(postConditionResults);

        assertThat(behaviours).hasSize(results.size())
                .extracting(Exploration::name)
                .containsAll(results.keySet());
    }

    private Map<Name, TestResult> wrapName(Map<String, TestResult> results) {
        return results.entrySet().stream().collect(Collectors.toMap(
                s -> new Name(s.getKey()), Map.Entry::getValue
        ));
    }


    @ParameterizedTest
    @MethodSource({"passingResults", "mixtedResults"})
    void passIfALeastOneDataPasses(Map<String, TestResult> results) {
        when(this.postConditionResults.resultByPostConditions()).thenReturn(wrapName(results));
        Stream<Exploration> behaviours = exploratoryTester.tests(this.postConditionResults);
        try {
            behaviours.forEach(t ->
                    t.check(e -> fail("should pass the tests but got " + e)));
        } catch (AssertionError e) {
            fail("No exception should be thrown but got : " + e);
        }
    }

    @ParameterizedTest
    @MethodSource("failingResults")
    void failPotentialBehaviourIfNotDataValidatesPredicate(Map<String, TestResult> results) {
        when(this.postConditionResults.resultByPostConditions()).thenReturn(wrapName(results));
        AtomicBoolean errorFound = new AtomicBoolean(false);
        exploratoryTester.tests(this.postConditionResults)
                .forEach(ex -> ex.check(e -> {
                    errorFound.set(true);
                    assertThat(e.message)
                            .contains("no data validates this behaviour");
                }));
        assertThat(errorFound.get()).isTrue();
    }

}
