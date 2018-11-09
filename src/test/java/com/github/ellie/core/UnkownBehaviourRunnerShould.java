package com.github.ellie.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.stream.Stream;

import static com.github.ellie.core.RunnerBuilderShould.IGNORE_RESULTS_CONSUMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnkownBehaviourRunnerShould {

    private Runner otherRunner;
    private Runner exploratoryRunner;
    private ExplorationResults results;

    @BeforeEach
    void createRunner() {
        otherRunner = mock(Runner.class);
        results = mock(ExplorationResults.class);
        when(results.dataThatBehaviours(Mockito.any())).thenReturn(new TestResult(Map.of()));
        exploratoryRunner = new UnkownBehaviourRunner(otherRunner);
    }

    @Test
    void keepsOtherRunnerTests() {
        ConditionTest test = ConditionTest.postConditionTest("test", () -> {
        });
        Mockito.when(otherRunner.tests(results,IGNORE_RESULTS_CONSUMER))
               .thenReturn(Stream.of(test));

        assertThat(exploratoryRunner.tests(results,IGNORE_RESULTS_CONSUMER)).contains(test);
    }

    @Test
    void addsUnknownBehaviourLast() {
        assertThat(exploratoryRunner.tests(results,IGNORE_RESULTS_CONSUMER)).extracting(b -> b.name)
                                             .last()
                                             .isEqualTo("Unknown post-condition");
    }


    @Test
    void failsIfAtLeastOneDataPassesNothing() {
        when(results.dataThatBehaviours(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL))));

        Assertions.assertThatThrownBy(() -> this.exploratoryRunner.tests(results,IGNORE_RESULTS_CONSUMER)
                                                                  .forEach(t -> t.test.run()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("At least one data has unknown post-condition")
                  .hasMessageContaining("2");
    }

    @Test
    void passesIfAllDataPassesSomeThing() {
        when(results.dataThatBehaviours(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(1), Stream.of(ConditionOutput.PASS),
                   ExplorationArguments.of(2), Stream.of(ConditionOutput.PASS,ConditionOutput.PASS)
        )));

        try {
            exploratoryRunner.tests(results,IGNORE_RESULTS_CONSUMER)
                             .forEach(t -> t.test.run());
        } catch (Exception e) {
            fail("All data passes something");
        }
    }

    private Answer<TestResult> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return MultipleBehaviourRunnerShould.filterFrom(data);
    }


}
