package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ellie.core.explorers.ExplorerFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnkownBehaviourExplorerShould {

    private Explorer otherExplorer;
    private UnkownBehaviourExplorer unkownBehaviourRunner;
    private Explorer.PostConditionResults results;
    private Explorer.PreConditionResults preConditionResults;
    public static final Consumer<Exploration.ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };

    @BeforeEach
    void createRunner() {
        otherExplorer = mock(Explorer.class);
        results = mock(Explorer.PostConditionResults.class);
        preConditionResults = mock(Explorer.PreConditionResults.class);
        when(results.matchOutputs(Mockito.any())).thenReturn(EMPTY_TEST_RESULT);
        unkownBehaviourRunner = new UnkownBehaviourExplorer(otherExplorer);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), (c) -> EMPTY_EXPLORATION_RESULT);
        Mockito.when(otherExplorer.explore(results))
                .thenReturn(Stream.of(test));

        assertThat(unkownBehaviourRunner.explore(results)).contains(test);
    }

    @Test
    void returnsTestResults() {
        TestResult testResult = EMPTY_TEST_RESULT;
        when(results.matchOutputs(Mockito.any()))
                .thenReturn(testResult);

        List<TestResult> checkedResults = unkownBehaviourRunner.explore(results)
                .map(e -> e.check(IGNORE_ERROR_MESSAGE).testResult)
                .collect(Collectors.toList());

        assertThat(checkedResults.get(0)).isSameAs(testResult);

    }

    @Test
    void computeCorrelation() {
        when(results.matchOutputs(Mockito.any()))
                .thenReturn(EMPTY_TEST_RESULT);
        Correlations expectedCorrelations = new Correlations();
        when(this.preConditionResults.correlationsWith(Mockito.any())).thenReturn(expectedCorrelations);

        List<Correlations> correlations = unkownBehaviourRunner.explore(results,preConditionResults)
                .map(e -> e.check(IGNORE_ERROR_MESSAGE).correlations)
                .collect(Collectors.toList());

        assertThat(correlations.get(0)).isSameAs(expectedCorrelations);

    }

    @Test
    void addsUnknownBehaviourLast() {
        assertThat(unkownBehaviourRunner.explore(results)).extracting(Exploration::name)
                .last()
                .isEqualTo("Unknown post-exploration");
    }


    @Test
    void failsIfAtLeastOneDataPassesNothing() {
        ExplorationArguments two = ExplorationArguments.of(2);
        when(results.matchOutputs(Mockito.any())).then(filterFrom(Map.of(two, List.of(ConditionOutput.FAIL))));

        AtomicBoolean errorFound = new AtomicBoolean(false);
        this.unkownBehaviourRunner.explore(results)
                .forEach(t -> t.check(errorMessage -> {
                    errorFound.set(true);
                    assertThat(errorMessage.message)
                            .contains("At least one data has unknown post-exploration");
                    assertThat(errorMessage.causes).contains(two);
                }));

        assertThat(errorFound.get()).isTrue();
    }

    @Test
    void passesIfAllDataPassesSomeThing() {
        when(results.matchOutputs(Mockito.any())).then(filterFrom(Map.of(ExplorationArguments.of(1), List.of(ConditionOutput.PASS),
                ExplorationArguments.of(2), List.of(ConditionOutput.PASS, ConditionOutput.PASS)
        )));

        try {
            unkownBehaviourRunner.explore(results)
                    .forEach(t -> t.check(e -> fail("No error should be found but got : " + e)));
        } catch (Exception e) {
            fail("All data passes something");
        }
    }


}
