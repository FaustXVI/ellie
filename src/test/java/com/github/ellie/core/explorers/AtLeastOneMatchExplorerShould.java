package com.github.ellie.core.explorers;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.explorers.ExplorerFixtures.IGNORE_ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AtLeastOneMatchExplorerShould {

    private static final TestResult PASSING_RESULTS = o ->
            Map.of(PASS, List.of(ExplorationArguments.of(1)))
                    .getOrDefault(o, Collections.emptyList());
    private static final TestResult FAILING_RESULTS = o ->
            Map.of(FAIL, List.of(ExplorationArguments.of(2)))
                    .getOrDefault(o, Collections.emptyList());
    private static final TestResult MIXTED_RESULTS = o ->
            Map.of(FAIL, List.of(ExplorationArguments.of(2)),
                    PASS, List.of(ExplorationArguments.of(1)))
                    .getOrDefault(o, Collections.emptyList());

    private Explorer.PostConditionResults postConditionResults;
    private Explorer.PreConditionResults preConditionResults;
    private Explorer exploratoryExplorer;

    @BeforeEach
    void createRunner() {
        postConditionResults = mock(Explorer.PostConditionResults.class);
        preConditionResults = mock(Explorer.PreConditionResults.class);
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
        when(postConditionResults.resultByName()).thenReturn(wrapName(results));

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
        when(this.postConditionResults.resultByName()).thenReturn(wrapName(results));
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
    void failIfNoDataValidatesPredicate(Map<String, TestResult> results) {
        when(this.postConditionResults.resultByName()).thenReturn(wrapName(results));
        AtomicBoolean errorFound = new AtomicBoolean(false);
        exploratoryExplorer.explore(this.postConditionResults)
                .forEach(ex -> ex.check(e -> {
                    errorFound.set(true);
                    assertThat(e.message)
                            .contains("no data validates this behaviour");
                }));
        assertThat(errorFound.get()).isTrue();
    }

    @ParameterizedTest
    @MethodSource({"passingResults", "mixtedResults", "failingResults"})
    void returnsTestResults(Map<String, TestResult> results) {
        when(this.postConditionResults.resultByName()).thenReturn(wrapName(results));
        Stream<Exploration> behaviours = exploratoryExplorer.explore(this.postConditionResults);
        List<TestResult> checkedResults = behaviours.map(t ->
                t.check(IGNORE_ERROR_MESSAGE).testResult).collect(Collectors.toList());
        assertThat(checkedResults).containsAll(results.values());
    }

    @ParameterizedTest
    @MethodSource({"passingResults", "mixtedResults", "failingResults"})
    void computeCorrelation(Map<String, TestResult> results) {
        when(this.postConditionResults.resultByName()).thenReturn(wrapName(results));
        when(this.preConditionResults.resultByName()).thenReturn(wrapName(Map.of("precondition", o -> List.of())));
        Stream<Exploration> behaviours = exploratoryExplorer.explore(this.postConditionResults, this.preConditionResults);
        List<Correlations> allCorrelations = behaviours.map(t ->
                t.check(IGNORE_ERROR_MESSAGE).correlations).collect(Collectors.toList());
        for (Correlations correlations : allCorrelations) {
            correlations.forEach(c -> {
                assertThat(c.name).isEqualTo("precondition");
                assertThat(c.value).isEqualTo(0d);
            });
        }
    }

}
