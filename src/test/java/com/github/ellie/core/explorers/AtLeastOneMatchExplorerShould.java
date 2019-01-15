package com.github.ellie.core.explorers;

import com.github.ellie.core.*;
import com.github.ellie.core.conditions.ConditionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AtLeastOneMatchExplorerShould {

    private static final TestResult PASSING_RESULTS = new TestResult(List.of(new ConditionResult(PASS, ExplorationArguments.of(1))));
    private static final TestResult FAILING_RESULTS = new TestResult(List.of(new ConditionResult(FAIL, ExplorationArguments.of(2))));
    private static final TestResult MIXTED_RESULTS = new TestResult(List.of(
            new ConditionResult(PASS, ExplorationArguments.of(1)),
            new ConditionResult(FAIL, ExplorationArguments.of(2)),
            new ConditionResult(IGNORED, ExplorationArguments.of(3))
    ));

    private Explorer.PostConditionResults postConditionResults;
    private Explorer exploratoryExplorer;

    @BeforeEach
    void createRunner() {
        postConditionResults = mock(Explorer.PostConditionResults.class);
        exploratoryExplorer = new AtLeastOneMatchExplorer();
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
                exploratoryExplorer.explore(postConditionResults);

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
        Stream<Exploration> behaviours = exploratoryExplorer.explore(this.postConditionResults);
        try {
            behaviours.forEach(t ->
                    t.check(e -> fail("should pass the explore but got " + e)));
        } catch (AssertionError e) {
            fail("No exception should be thrown but got : " + e);
        }
    }

    @ParameterizedTest
    @MethodSource("failingResults")
    void failPotentialBehaviourIfNotDataValidatesPredicate(Map<String, TestResult> results) {
        when(this.postConditionResults.resultByPostConditions()).thenReturn(wrapName(results));
        AtomicBoolean errorFound = new AtomicBoolean(false);
        exploratoryExplorer.explore(this.postConditionResults)
                .forEach(ex -> ex.check(e -> {
                    errorFound.set(true);
                    assertThat(e.message)
                            .contains("no data validates this behaviour");
                }));
        assertThat(errorFound.get()).isTrue();
    }

}
