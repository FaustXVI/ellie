package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.explorers.ExplorerFixtures.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipleBehaviourExplorerShould {

    public static final Map<ExplorationArguments, Collection<ConditionOutput>>
            NO_MULTIPLE_PASS = Map.of(ExplorationArguments.of(1), List.of(PASS),
            ExplorationArguments.of(2), List.of(ConditionOutput.FAIL));
    public static final Map<ExplorationArguments, Collection<ConditionOutput>>
            MULTIPLE_PASS = Map.of(ExplorationArguments.of(2), List.of(PASS, PASS));
    private static final Map<ExplorationArguments, Collection<ConditionOutput>> MIXED = new HashMap<>() {{
        putAll(NO_MULTIPLE_PASS);
        putAll(MULTIPLE_PASS);
    }};
    private Explorer otherExplorer;
    private MultipleBehaviourExplorer multipleBehaviourRunner;
    private Explorer.PostConditionResults results;
    private Explorer.PreConditionResults preConditionResults;

    @BeforeEach
    void createRunner() {
        otherExplorer = mock(Explorer.class);
        results = mock(Explorer.PostConditionResults.class);
        preConditionResults = mock(Explorer.PreConditionResults.class);
        when(results.matchOutputs(Mockito.any())).thenReturn(EMPTY_TEST_RESULT);
        multipleBehaviourRunner = new MultipleBehaviourExplorer(otherExplorer);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), (c) -> EMPTY_EXPLORATION_RESULT);
        Mockito.when(otherExplorer.explore(results))
                .thenReturn(Stream.of(test));

        assertThat(multipleBehaviourRunner.explore(results)).contains(test);
    }

    @Test
    void addsMultipleBehaviourLast() {
        assertThat(multipleBehaviourRunner.explore(results))
                .extracting(Exploration::name)
                .last()
                .isEqualTo(
                        "Match multiple post-conditions");
    }

    @Test
    void failsIfAtLeastOneDataPassesManyTimes() {
        when(results.matchOutputs(Mockito.any())).then(ExplorerFixtures.filterFrom(MULTIPLE_PASS));

        AtomicBoolean errorFound = new AtomicBoolean(false);

        this.multipleBehaviourRunner.explore(results)
                .forEach(t -> t.check(errorMessage -> {
                    errorFound.set(true);
                    assertThat(errorMessage.message).contains("one data has many post-conditions");
                    assertThat(errorMessage.causes).containsAll(MULTIPLE_PASS.keySet());
                }));

        assertThat(errorFound.get()).isTrue();
    }

    @Test
    void passesIfNoDataPassesManyTimes() {
        when(results.matchOutputs(Mockito.any()))
                .then(ExplorerFixtures.filterFrom(NO_MULTIPLE_PASS));

        try {
            multipleBehaviourRunner.explore(results)
                    .forEach(t -> t.check(e -> fail("No error should be found but got : " + e)));
        } catch (Exception e) {
            fail("No data passes many post conditions");
        }
    }

    @Test
    void returnsTestResults() {
        when(results.matchOutputs(Mockito.any()))
                .thenReturn(EMPTY_TEST_RESULT);

        List<TestResult> checkedResults = multipleBehaviourRunner.explore(results)
                .map(t -> t.check(IGNORE_ERROR_MESSAGE).testResult)
                .collect(toList());

        assertThat(checkedResults.get(0)).isSameAs(EMPTY_TEST_RESULT);
    }

    @Test
    void computeCorrelation() {
        when(results.matchOutputs(Mockito.any()))
                .thenReturn(EMPTY_TEST_RESULT);
        Correlations expectedCorrelations = new Correlations();
        when(this.preConditionResults.correlationsWith(Mockito.any())).thenReturn(expectedCorrelations);
        List<Correlations> correlations = multipleBehaviourRunner.explore(results, preConditionResults)
                .map(t -> t.check(IGNORE_ERROR_MESSAGE).correlations)
                .collect(toList());

        assertThat(correlations.get(0)).isSameAs(expectedCorrelations);
    }


}
