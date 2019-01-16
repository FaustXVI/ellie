package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnkownBehaviourExplorerShould {

    public static final TestResult EMPTY_TEST_RESULT = o -> Collections.emptyList();
    private Explorer otherExplorer;
    private UnkownBehaviourExplorer unkownBehaviourRunner;
    private Explorer.PostConditionResults results;
    public static final Consumer<Exploration.ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };

    @BeforeEach
    void createRunner() {
        otherExplorer = mock(Explorer.class);
        results = mock(Explorer.PostConditionResults.class);
        when(results.matchOutputs(Mockito.any())).thenReturn(EMPTY_TEST_RESULT);
        unkownBehaviourRunner = new UnkownBehaviourExplorer(otherExplorer);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), (c) -> EMPTY_TEST_RESULT);
        Mockito.when(otherExplorer.explore(results))
                .thenReturn(Stream.of(test));

        assertThat(unkownBehaviourRunner.explore(results)).contains(test);
    }

    @Test
    void callsConsumerWithResults() {
        TestResult TestResult = EMPTY_TEST_RESULT;
        when(results.matchOutputs(Mockito.any()))
                .thenReturn(TestResult);

        unkownBehaviourRunner.explore(results).forEach(ct -> {
            assertThat(ct.name()).isEqualTo("Unknown post-exploration");
            assertThat(ct.check(IGNORE_ERROR_MESSAGE)).isSameAs(TestResult);
        });

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
        when(results.matchOutputs(Mockito.any())).then(filterFrom(
                Map.of(two, Stream.of(ConditionOutput.FAIL))));

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
        when(results.matchOutputs(Mockito.any())).then(filterFrom(
                Map.of(ExplorationArguments.of(1), Stream.of(ConditionOutput.PASS),
                        ExplorationArguments.of(2), Stream.of(ConditionOutput.PASS, ConditionOutput.PASS)
                )));

        try {
            unkownBehaviourRunner.explore(results)
                    .forEach(t -> t.check(e -> fail("No error should be found but got : " + e)));
        } catch (Exception e) {
            fail("All data passes something");
        }
    }

    private Answer<TestResult> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return MultipleBehaviourExplorerShould.filterFrom(data);
    }


}
