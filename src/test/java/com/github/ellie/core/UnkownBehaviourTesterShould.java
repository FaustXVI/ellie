package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;
import com.github.ellie.core.asserters.Tester;
import com.github.ellie.core.asserters.UnkownBehaviourTester;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static com.github.ellie.core.ExploratoryTesterShould.IGNORE_RESULTS_CONSUMER;
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
        when(results.dataThatPostConditions(Mockito.any())).thenReturn(new TestResult(Map.of()));
        unkownBehaviourRunner = new UnkownBehaviourTester(otherTester);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), () -> {
        });
        Mockito.when(otherTester.tests(results, IGNORE_RESULTS_CONSUMER))
               .thenReturn(Stream.of(test));

        assertThat(unkownBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)).contains(test);
    }

    @Test
    void callsConsumerWithResults() {
        TestResult testResult = new TestResult(Map.of());
        when(results.dataThatPostConditions(Mockito.any()))
            .thenReturn(testResult);

        AtomicBoolean consumerExecuted = new AtomicBoolean(false);

        unkownBehaviourRunner.tests(results, (a, b) -> {
            assertThat(a).isEqualTo("Unknown post-exploration");
            assertThat(b).isSameAs(testResult);
            consumerExecuted.set(true);
        }).forEach(ct -> ct.test.run());

        if (!consumerExecuted.get()) {
            fail("Consumer should be called");
        }
    }

    @Test
    void addsUnknownBehaviourLast() {
        assertThat(unkownBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)).extracting(b -> b.name.value)
                                                                                 .last()
                                                                                 .isEqualTo("Unknown post-exploration");
    }


    @Test
    void failsIfAtLeastOneDataPassesNothing() {
        when(results.dataThatPostConditions(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL))));

        Assertions.assertThatThrownBy(() -> this.unkownBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)
                                                                      .forEach(t -> t.test.run()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("At least one data has unknown post-exploration")
                  .hasMessageContaining("2");
    }

    @Test
    void passesIfAllDataPassesSomeThing() {
        when(results.dataThatPostConditions(Mockito.any())).then(filterFrom(
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
