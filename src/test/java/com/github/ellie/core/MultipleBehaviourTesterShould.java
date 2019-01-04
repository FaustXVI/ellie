package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;
import com.github.ellie.core.asserters.MultipleBehaviourTester;
import com.github.ellie.core.asserters.Tester;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.ExploratoryTesterShould.IGNORE_RESULTS_CONSUMER;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipleBehaviourTesterShould {

    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
        NO_MULTIPLE_PASS = Map.of(ExplorationArguments.of(1), Stream.of(PASS),
                                  ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL),
                                  ExplorationArguments.of(3), Stream.of(ConditionOutput.IGNORED));
    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
        MULTIPLE_PASS = Map.of(ExplorationArguments.of(2), Stream.of(PASS, PASS));
    private Tester otherTester;
    private MultipleBehaviourTester multipleBehaviourRunner;
    private ExplorationResults results;

    @BeforeEach
    void createRunner() {
        otherTester = mock(Tester.class);
        results = mock(ExplorationResults.class);
        when(results.dataThatPostConditions(Mockito.any())).thenReturn(new TestResult(Map.of()));
        multipleBehaviourRunner = new MultipleBehaviourTester(otherTester);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), () -> {
        });
        Mockito.when(otherTester.tests(results, IGNORE_RESULTS_CONSUMER))
               .thenReturn(Stream.of(test));

        assertThat(multipleBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)).contains(test);
    }

    @Test
    void addsMultipleBehaviourLast() {
        assertThat(multipleBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER))
            .extracting(b -> b.name.value)
            .last()
            .isEqualTo(
                "Match multiple "
                + "post-conditions");
    }


    @Test
    void callsConsumerWithResults() {
        TestResult testResult = new TestResult(Map.of());
        when(results.dataThatPostConditions(Mockito.any()))
            .thenReturn(testResult);

        AtomicBoolean consumerExecuted = new AtomicBoolean(false);

        multipleBehaviourRunner.tests(results, (a, b) -> {
            assertThat(a).isEqualTo("Match multiple post-conditions");
            assertThat(b).isSameAs(testResult);
            consumerExecuted.set(true);
        }).forEach(ct -> ct.test.run());

        if (!consumerExecuted.get()) {
            fail("Consumer should be called");
        }
    }

    @Test
    void failsIfAtLeastOneDataPassesManyTimes() {
        when(results.dataThatPostConditions(Mockito.any())).then(filterFrom(MULTIPLE_PASS));

        Assertions.assertThatThrownBy(() -> this.multipleBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)
                                                                        .forEach(t -> t.test.run()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has many post-conditions")
                  .hasMessageContaining("2");
    }

    @Test
    void passesIfNoDataPassesManyTimes() {
        when(results.dataThatPostConditions(Mockito.any()))
            .then(filterFrom(NO_MULTIPLE_PASS));

        try {
            multipleBehaviourRunner.tests(results, IGNORE_RESULTS_CONSUMER)
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
