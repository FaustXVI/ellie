package com.github.ellie.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.github.ellie.core.TesterBuilderShould.IGNORE_RESULTS_CONSUMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnkownBehaviourTesterShould {

    private Tester otherTester;
    private UnkownBehaviourTester unkownBehaviourRunner;
    private ExplorationResults results;

    @BeforeEach
    void createRunner() {
        otherTester = mock(Tester.class);
        results = mock(ExplorationResults.class);
        when(results.dataThatBehaviours(Mockito.any())).thenReturn(new TestResult(Map.of()));
        unkownBehaviourRunner = new UnkownBehaviourTester(otherTester);
    }

    @Test
    void keepsOtherRunnerTests() {
        ConditionTest test = ConditionTest.postConditionTest("test", () -> {
        });
        Mockito.when(otherTester.tests(results, IGNORE_RESULTS_CONSUMER))
               .thenReturn(Stream.of(test));

        assertThat(unkownBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)).contains(test);
    }

    @Test
    void callsConsumerWithResults() {
        TestResult testResult = new TestResult(Map.of());
        when(results.dataThatBehaviours(Mockito.any()))
            .thenReturn(testResult);

        AtomicBoolean consumerExecuted = new AtomicBoolean(false);

        unkownBehaviourRunner.tests(results, (a, b) -> {
            assertThat(a).isEqualTo("Unknown post-condition");
            assertThat(b).isSameAs(testResult);
            consumerExecuted.set(true);
        }).forEach(ct -> ct.test.run());

        if (!consumerExecuted.get()) {
            fail("Consumer should be called");
        }
    }

    @Test
    void addsUnknownBehaviourLast() {
        assertThat(unkownBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)).extracting(b -> b.name)
                                                                                 .last()
                                                                                 .isEqualTo("Unknown post-condition");
    }


    @Test
    void failsIfAtLeastOneDataPassesNothing() {
        when(results.dataThatBehaviours(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL))));

        Assertions.assertThatThrownBy(() -> this.unkownBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)
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
            unkownBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)
                                 .forEach(t -> t.test.run());
        } catch (Exception e) {
            fail("All data passes something");
        }
    }

    private Answer<TestResult> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return MultipleBehaviourTesterShould.filterFrom(data);
    }


}
