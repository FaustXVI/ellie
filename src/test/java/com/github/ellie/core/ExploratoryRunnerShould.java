package com.github.ellie.core;

import com.github.ellie.examples.invalids.NoDataExploration;
import com.github.ellie.examples.invalids.NotIterableDataExploration;
import com.github.ellie.examples.invalids.NotPredicateExploration;
import com.github.ellie.examples.invalids.TwoBehaviourExploration;
import com.github.ellie.examples.valids.AllAllowedTypesExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionWithConsumersExploration;
import com.github.ellie.examples.valids.OneSuppositionExploration;
import com.github.ellie.examples.valids.PerfectSuppositionExploration;
import com.github.ellie.examples.valids.PerfectSuppositionWithAssumtionsExploration;
import com.github.ellie.examples.valids.ProtectedMethodsExploration;
import com.github.ellie.examples.valids.TwoDataProviderExploration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExploratoryRunnerShould {

    private static final BiConsumer<String, TestResult> IGNORE_PASSING_CASES_CONSUMER = (l, o) -> {
    };
    public static final TestResult EMPTY_RESULTS = new TestResult(Map.of());
    private Explorer explorer;
    private Runner exploratoryRunner;

    @BeforeEach
    void createRunner() {
        explorer = mock(Explorer.class);
        exploratoryRunner = new ExploratoryRunner(explorer, IGNORE_PASSING_CASES_CONSUMER);
    }

    static Stream<Arguments> results() {
        return Stream.of(
            Arguments.of(Map.of("b1", EMPTY_RESULTS)),
            Arguments.of(Map.of("b1", EMPTY_RESULTS, "b2", EMPTY_RESULTS))
        );
    }

    @ParameterizedTest
    @MethodSource("results")
    void createsOneTestPerPostCondition(Map<String, TestResult> results) {
        when(explorer.resultByBehaviour()).thenReturn(results);

        Stream<ConditionTest> behaviours = exploratoryRunner.tests();

        assertThat(behaviours).hasSize(results.size())
                              .extracting(c -> c.name)
                              .containsAll(results.keySet());
    }

    @Test
        // TODO : should be the responsability of the runner
    void testEachBehavioursWithEachData() {
        OneSuppositionExploration testInstance = Mockito.spy(new OneSuppositionExploration());
        exploratoryRunnerFor(testInstance)
                                          .forEach(t -> {
                                              try {
                                                  t.test.run();
                                              } catch (Throwable throwable) {
                                                  //test result is not the concern here
                                              }
                                          });
        verify(testInstance).numbers();
        verify(testInstance, atLeast(1)).times2(2);
        verify(testInstance, atLeast(1)).times2(4);
        verify(testInstance, atLeast(1)).is4(2);
        verify(testInstance, atLeast(1)).is4(4);
    }

    @Test
        // TODO should be the responsability of the runner
    void combinesAllDataProviderMethods() {
        TwoDataProviderExploration testInstance = Mockito.spy(new TwoDataProviderExploration());
        exploratoryRunnerFor(testInstance)
                                          .forEach(t -> {
                                              try {
                                                  t.test.run();
                                              } catch (Throwable throwable) {
                                                  //test result is not the concern here
                                              }
                                          });
        verify(testInstance).two();
        verify(testInstance).four();
        verify(testInstance, atLeast(1)).times2(2);
        verify(testInstance, atLeast(1)).times2(4);
        verify(testInstance, atLeast(1)).is4(2);
        verify(testInstance, atLeast(1)).is4(4);
    }


    @Test
    void callsConsumerWithTestResultAfterRun() {
        Map<String, TestResult> testResults = new HashMap<>();
        OneSuppositionExploration testInstance = new OneSuppositionExploration();

        RunnerBuilder.generateTestsFor(testInstance, testResults::put)
                     .forEach(t -> {
                             try {
                                 t.test
                                     .run();
                             } catch (Throwable throwable) {
                                 //test result is not the concern here
                             }
                         });

        String passingSupposition = "times2_is4";
        assertThat(testResults).containsOnlyKeys(passingSupposition);
        TestResult testResult = testResults.get(passingSupposition);
        assertThat(testResult.passingData())
            .extracting(ExplorationArguments::get)
            .containsExactly(new Object[]{2});
        assertThat(testResult.failingData())
            .extracting(ExplorationArguments::get)
            .containsExactly(new Object[]{4});
    }

    @Test
    void passPotentialBehaviourIfALeastOneDataValidatesPredicate() {
        Stream<ConditionTest> behaviours = new ExploratoryRunner(new Explorer(new OneSuppositionExploration()), IGNORE_PASSING_CASES_CONSUMER).tests();
        try {
            behaviours.forEach(t -> t.test.run());
        } catch (AssertionError e) {
            fail("No exception should be thrown but got : "+e);
        }
    }

    static Stream<Arguments> allWrong() {
        return Stream.of(
            Arguments.of(new AllWrongSuppositionExploration()),
            Arguments.of(new AllWrongSuppositionWithConsumersExploration())
        );
    }

    @ParameterizedTest
    @MethodSource("allWrong")
    void failPotentialBehaviourIfNotDataValidatesPredicate(Object testInstance) {
        Stream<ConditionTest> behaviours = exploratoryRunnerFor(testInstance);

        Assertions.assertThatThrownBy(() -> behaviours.forEach(t -> t.test.run()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("no data validates this behaviour");
    }


    static Stream<Arguments> perfectExplorations() {
        return Stream.of(
            Arguments.of(new PerfectSuppositionExploration()),
            Arguments.of(new PerfectSuppositionWithAssumtionsExploration()),
            Arguments.of(new AllAllowedTypesExploration()),
            Arguments.of(new ProtectedMethodsExploration())
        );
    }

    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void passAllTestsIfPerfectComprehension(Object testInstance) {
        List<ConditionTest> behaviours =
            exploratoryRunnerFor(testInstance)
                                              .collect(Collectors.toList());

        try {
            for (ConditionTest behaviour : behaviours) {
                behaviour.test.run();
            }
        } catch (Throwable throwable) {
            fail("Perfect comprehension should be green", throwable);
        }
    }

    @Test
        // TODO : should create a class analyzer ?
    void throwsAnExceptionIfMoreThanOneBehaviourIsExplored() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new TwoBehaviourExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfNoDataIsGiven() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new NoDataExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfDataIsNotIterableOrStream() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new NotIterableDataExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfPotentialBehaviourIsNotPredicateOrConsumer() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new NotPredicateExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    public static Stream<ConditionTest> exploratoryRunnerFor(Object testInstance) {
        return RunnerBuilder.generateTestsFor(testInstance,IGNORE_PASSING_CASES_CONSUMER);
    }

}
