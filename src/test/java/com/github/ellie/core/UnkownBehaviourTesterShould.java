package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;
import com.github.ellie.core.asserters.Tester;
import com.github.ellie.core.asserters.UnkownBehaviourTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnkownBehaviourTesterShould {

    private Tester otherTester;
    private UnkownBehaviourTester unkownBehaviourRunner;
    private PostConditionResults results;
    public static final Consumer<ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };

    @BeforeEach
    void createRunner() {
        otherTester = mock(Tester.class);
        results = mock(PostConditionResults.class);
        when(results.dataThatPostConditions(Mockito.any())).thenReturn(new TestResult(Map.of()));
        unkownBehaviourRunner = new UnkownBehaviourTester(otherTester);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), (c) -> new TestResult(new ArrayList<>()));
        Mockito.when(otherTester.tests(results))
                .thenReturn(Stream.of(test));

        assertThat(unkownBehaviourRunner.tests(results)).contains(test);
    }

    @Test
    void callsConsumerWithResults() {
        TestResult testResult = new TestResult(Map.of());
        when(results.dataThatPostConditions(Mockito.any()))
                .thenReturn(testResult);

        unkownBehaviourRunner.tests(results).forEach(ct -> {
            assertThat(ct.name.value).isEqualTo("Unknown post-exploration");
            assertThat(ct.test.check(IGNORE_ERROR_MESSAGE)).isSameAs(testResult);
        });

    }

    @Test
    void addsUnknownBehaviourLast() {
        assertThat(unkownBehaviourRunner.tests(results)).extracting(b -> b.name.value)
                .last()
                .isEqualTo("Unknown post-exploration");
    }


    @Test
    void failsIfAtLeastOneDataPassesNothing() {
        when(results.dataThatPostConditions(Mockito.any())).then(filterFrom(
                Map.of(ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL))));
        AtomicReference<ErrorMessage> errorMessageAtomicReference = new AtomicReference<>();
        TestResult testResult = this.unkownBehaviourRunner.tests(results)
                .map(t -> t.test.check(errorMessageAtomicReference::set))
                .findFirst().get();
        ErrorMessage errorMessage = errorMessageAtomicReference.get();
        assertThat(errorMessage.message)
                .contains("At least one data has unknown post-exploration");
        assertThat(errorMessage.causes).contains(ExplorationArguments.of(2));
    }

    @Test
    void passesIfAllDataPassesSomeThing() {
        when(results.dataThatPostConditions(Mockito.any())).then(filterFrom(
                Map.of(ExplorationArguments.of(1), Stream.of(ConditionOutput.PASS),
                        ExplorationArguments.of(2), Stream.of(ConditionOutput.PASS, ConditionOutput.PASS)
                )));

        try {
            unkownBehaviourRunner.tests(results)
                    .forEach(t -> t.test.check(IGNORE_ERROR_MESSAGE));
        } catch (Exception e) {
            fail("All data passes something");
        }
    }

    private Answer<TestResult> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return MultipleBehaviourTesterShould.filterFrom(data);
    }


}
