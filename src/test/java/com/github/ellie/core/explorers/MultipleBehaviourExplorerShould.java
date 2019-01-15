package com.github.ellie.core.explorers;

import com.github.ellie.core.*;
import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.conditions.ConditionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipleBehaviourExplorerShould {

    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
            NO_MULTIPLE_PASS = Map.of(ExplorationArguments.of(1), Stream.of(PASS),
            ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL),
            ExplorationArguments.of(3), Stream.of(ConditionOutput.IGNORED));
    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
            MULTIPLE_PASS = Map.of(ExplorationArguments.of(2), Stream.of(PASS, PASS));
    private Explorer otherExplorer;
    private MultipleBehaviourExplorer multipleBehaviourRunner;
    private Explorer.PostConditionResults results;
    private static final Consumer<Exploration.ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };


    @BeforeEach
    void createRunner() {
        otherExplorer = mock(Explorer.class);
        results = mock(Explorer.PostConditionResults.class);
        when(results.dataThatPostConditions(Mockito.any())).thenReturn(new TestResult(List.of()));
        multipleBehaviourRunner = new MultipleBehaviourExplorer(otherExplorer);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), (c) -> new TestResult(new ArrayList<>()));
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
    void callsConsumerWithResults() {
        TestResult testResult = new TestResult(List.of());
        when(results.dataThatPostConditions(Mockito.any()))
                .thenReturn(testResult);

        multipleBehaviourRunner.explore(results).forEach(ct -> {
            assertThat(ct.name()).isEqualTo("Match multiple post-conditions");
            assertThat(ct.check(IGNORE_ERROR_MESSAGE)).isSameAs(testResult);
        });

    }

    @Test
    void failsIfAtLeastOneDataPassesManyTimes() {
        when(results.dataThatPostConditions(Mockito.any())).then(filterFrom(MULTIPLE_PASS));

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
        when(results.dataThatPostConditions(Mockito.any()))
                .then(filterFrom(NO_MULTIPLE_PASS));

        try {
            multipleBehaviourRunner.explore(results)
                    .forEach(t -> t.check(e -> fail("No error should be found but got : " + e)));
        } catch (Exception e) {
            fail("No data passes many post conditions");
        }
    }

    public static Answer<TestResult> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return invocationOnMock -> {
            Predicate<Stream<ConditionOutput>> predicate = invocationOnMock.getArgument(0);
            List<ConditionResult> results =
                    data.entrySet()
                            .stream()
                            .map(e -> new ConditionResult(predicate.test(e.getValue()) ? PASS
                                    : FAIL, e.getKey()))
                            .collect(toList());
            return new TestResult(results);
        };
    }


}