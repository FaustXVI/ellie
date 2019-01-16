package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import com.github.ellie.core.conditions.ConditionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipleBehaviourExplorerShould {

    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
            NO_MULTIPLE_PASS = Map.of(ExplorationArguments.of(1), Stream.of(PASS),
            ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL));
    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
            MULTIPLE_PASS = Map.of(ExplorationArguments.of(2), Stream.of(PASS, PASS));
    public static final TestResult EMPTY_TEST_RESULT = o -> Collections.emptyList();
    private Explorer otherExplorer;
    private MultipleBehaviourExplorer multipleBehaviourRunner;
    private Explorer.PostConditionResults results;
    private static final Consumer<Exploration.ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };


    @BeforeEach
    void createRunner() {
        otherExplorer = mock(Explorer.class);
        results = mock(Explorer.PostConditionResults.class);
        when(results.matchOutputs(Mockito.any())).thenReturn(EMPTY_TEST_RESULT);
        multipleBehaviourRunner = new MultipleBehaviourExplorer(otherExplorer);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), (c) -> EMPTY_TEST_RESULT);
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
        TestResult TestResult = EMPTY_TEST_RESULT;
        when(results.matchOutputs(Mockito.any()))
                .thenReturn(TestResult);

        multipleBehaviourRunner.explore(results).forEach(ct -> {
            assertThat(ct.name()).isEqualTo("Match multiple post-conditions");
            assertThat(ct.check(IGNORE_ERROR_MESSAGE)).isSameAs(TestResult);
        });

    }

    @Test
    void failsIfAtLeastOneDataPassesManyTimes() {
        when(results.matchOutputs(Mockito.any())).then(filterFrom(MULTIPLE_PASS));

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
            return o -> results.stream().filter(r -> o == r.output).map(r -> r.arguments).collect(toList());
        };
    }


}
