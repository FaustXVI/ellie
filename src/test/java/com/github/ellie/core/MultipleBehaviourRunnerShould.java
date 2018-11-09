package com.github.ellie.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.RunnerBuilderShould.IGNORE_RESULTS_CONSUMER;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipleBehaviourRunnerShould {

    private Runner otherRunner;
    private Runner exploratoryRunner;
    private ExplorationResults results;

    @BeforeEach
    void createRunner() {
        otherRunner = mock(Runner.class);
        results = mock(ExplorationResults.class);
        when(results.dataThatBehaviours(Mockito.any())).thenReturn(new TestResult(Map.of()));
        exploratoryRunner = new MultipleBehaviourRunner(otherRunner);
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
    void addsMultipleBehaviourLast() {
        assertThat(exploratoryRunner.tests(results,IGNORE_RESULTS_CONSUMER)).extracting(b -> b.name)
                                                    .last()
                                                    .isEqualTo("Match multiple post-conditions");
    }


    @Test
    void failsIfAtLeastOneDataPassesManyTimes() {
        when(results.dataThatBehaviours(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(2), Stream.of(PASS, PASS))));

        Assertions.assertThatThrownBy(() -> this.exploratoryRunner.tests(results,IGNORE_RESULTS_CONSUMER)
                                                                  .forEach(t -> t.test.run()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has many post-conditions")
                  .hasMessageContaining("2");
    }

    @Test
    void passesIfNoDataPassesManyTimes() {
        when(results.dataThatBehaviours(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(1), Stream.of(PASS),
                   ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL),
                   ExplorationArguments.of(3), Stream.of(ConditionOutput.IGNORED))
        ));

        try {
            exploratoryRunner.tests(results,IGNORE_RESULTS_CONSUMER)
                             .forEach(t -> t.test.run());
        } catch (Exception e) {
            fail("No data passes many post conditions");
        }
    }

    public static Answer<TestResult> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return invocationOnMock -> {
            Predicate<Stream<ConditionOutput>> predicate = invocationOnMock.getArgument(0);
            Map<ConditionOutput, List<ExplorationArguments>> results =
                data.entrySet()
                    .stream()
                    .collect(groupingBy(
                        e -> predicate.test(e.getValue()) ? PASS
                                                          : FAIL,
                        mapping(Map.Entry::getKey,
                                           toList())
                    ));
            return new TestResult(results);
        };
    }


}
