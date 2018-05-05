package com.github.ellie.core;

import com.github.ellie.examples.invalids.NoDataExploration;
import com.github.ellie.examples.invalids.NotIterableDataExploration;
import com.github.ellie.examples.invalids.NotPredicateExploration;
import com.github.ellie.examples.invalids.TwoBehaviourExploration;
import com.github.ellie.examples.valids.AllAllowedTypesExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionWithConsumersExploration;
import com.github.ellie.examples.valids.DataMatchesMultipleSuppositionExploration;
import com.github.ellie.examples.valids.MultipleExplorationArgumentsExploration;
import com.github.ellie.examples.valids.OneSuppositionExploration;
import com.github.ellie.examples.valids.PerfectSuppositionExploration;
import com.github.ellie.examples.valids.PerfectSuppositionWithAssumtionsExploration;
import com.github.ellie.examples.valids.ProtectedMethodsExploration;
import com.github.ellie.examples.valids.TwoDataProviderExploration;
import com.github.ellie.examples.valids.TwoSuppositionExploration;
import com.github.ellie.examples.valids.ZeroSuppositionExploration;
import org.assertj.core.api.Assertions;
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
import static org.mockito.Mockito.verify;

public class ExploratoryRunnerShould {

    private static final BiConsumer<String, TestResult> IGNORE_PASSING_CASES_CONSUMER = (l, o) -> {
    };

    static Stream<Arguments> numberOfBehaviours() {
        return Stream.of(
            Arguments.of(new ZeroSuppositionExploration(), 0),
            Arguments.of(new OneSuppositionExploration(), 1),
            Arguments.of(new TwoSuppositionExploration(), 2),
            Arguments.of(new MultipleExplorationArgumentsExploration(), 1)
        );
    }

    @ParameterizedTest
    @MethodSource("numberOfBehaviours")
    void createOneNodePerPotentialBehaviorAndThreeMoreForUndefinedBehaviourAndMultipleAndPerfect(Object testInstance,
                                                                                     int numberOfBehaviours) {
        Stream<PostConditionTest> behaviours = exploratoryRunnerFor(testInstance).tests();
        assertThat(behaviours).hasSize(numberOfBehaviours + 3);
    }


    static Stream<Arguments> methodNames() {
        return Stream.of(
            Arguments.of(new OneSuppositionExploration(), "times2", List.of("is4")),
            Arguments.of(new TwoSuppositionExploration(), "times3", List.of("is6", "is16"))
        );
    }

    @ParameterizedTest
    @MethodSource("methodNames")
    void nameNodesWithActionAndSupposedBehaviour(Object testInstance, String actionName,
                                                 List<String> behaviourNames) {
        Stream<PostConditionTest> behaviours = exploratoryRunnerFor(testInstance).testedBehaviours();

        assertThat(behaviours).extracting(b -> b.name)
                              .containsAll(behaviourNames.stream()
                                                         .map(behaviourName -> actionName + "_" + behaviourName)
                                                         .collect(Collectors.toList()))
        ;
    }

    static Stream<Arguments> testInstances() {
        return Stream.of(
            Arguments.of(new ZeroSuppositionExploration()),
            Arguments.of(new OneSuppositionExploration()),
            Arguments.of(new TwoSuppositionExploration())
        );
    }

    @ParameterizedTest
    @MethodSource("testInstances")
    void addUnknownBehaviourSecondLast(Object testInstance) {
        List<PostConditionTest> behaviours = exploratoryRunnerFor(testInstance).tests()
                                                                               .collect(Collectors.toList());

        assertThat(behaviours).extracting(b -> b.name)
                              .element(behaviours.size() - 2)
                              .isEqualTo("Unknown post-condition");
    }

    @ParameterizedTest
    @MethodSource("testInstances")
    void addPerfectDefinitionLast(Object testInstance) {
        List<PostConditionTest> behaviours = exploratoryRunnerFor(testInstance).tests()
                                                                               .collect(Collectors.toList());

        assertThat(behaviours).extracting(b -> b.name)
                              .element(behaviours.size() - 1)
                              .isEqualTo("Perfect definition");
    }


    @ParameterizedTest
    @MethodSource("testInstances")
    void addsMultipleBehaviourThirdLast(Object testInstance) {
        List<PostConditionTest> behaviours = exploratoryRunnerFor(testInstance).tests()
                                                                               .collect(Collectors.toList());

        assertThat(behaviours).extracting(b -> b.name)
                              .element(behaviours.size() - 3)
                              .isEqualTo("Match multiple post-conditions");
    }

    @Test
    void testEachBehavioursWithEachData() {
        OneSuppositionExploration testInstance = Mockito.spy(new OneSuppositionExploration());
        exploratoryRunnerFor(testInstance).testedBehaviours()
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
    void combinesAllDataProviderMethods() {
        TwoDataProviderExploration testInstance = Mockito.spy(new TwoDataProviderExploration());
        exploratoryRunnerFor(testInstance).testedBehaviours()
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

        ExploratoryRunner.generateTestsFor(testInstance, testResults::put)
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


    static Stream<Arguments> allWrong() {
        return Stream.of(
            Arguments.of(new AllWrongSuppositionExploration()),
            Arguments.of(new AllWrongSuppositionWithConsumersExploration())
        );
    }

    @Test
    void passPotentialBehaviourIfALeastOneDataValidatesPredicate() {
        Stream<PostConditionTest> behaviours = exploratoryRunnerFor(new OneSuppositionExploration()).testedBehaviours();
        try {
            behaviours.forEach(t -> t.test.run());
        } catch (AssertionError e) {
            fail("No exception should be thrown");
        }
    }

    @ParameterizedTest
    @MethodSource("allWrong")
    void failPotentialBehaviourIfNotDataValidatesPredicate(Object testInstance) {
        Stream<PostConditionTest> behaviours = exploratoryRunnerFor(testInstance).testedBehaviours();

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
    void passUnknownBehaviourIfAllDataValidatesAtLeastPredicate(Object testInstance) {
        try {
            exploratoryRunnerFor(testInstance).unknownBehaviour().test.run();
        } catch (Throwable throwable) {
            fail("All behaviour are found, no unknown behaviour should be left", throwable);
        }
    }

    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void passAllTestsIfPerfectComprehension(Object testInstance) {
        List<PostConditionTest> behaviours =
            exploratoryRunnerFor(testInstance).tests()
                                              .collect(Collectors.toList());

        try {
            for (PostConditionTest behaviour : behaviours) {
                behaviour.test.run();
            }
        } catch (Throwable throwable) {
            fail("Perfect comprehension should be green", throwable);
        }
    }

    @Test
    void failUnknownBehaviourIfAtLeastOneDataValidatesAnyPredicateAndLogIt() {
        PostConditionTest behaviour =
            exploratoryRunnerFor(new AllWrongSuppositionExploration()).unknownBehaviour();

        Assertions.assertThatThrownBy(behaviour.test::run)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has unknown post-condition")
                  .hasMessageContaining("2")
                  .hasMessageContaining("4");
    }

    @Test
    void failMultipleBehaviourIfAtLeastOneDataValidatesMultiplePredicateAndLogIt() {
        PostConditionTest behaviour =
            exploratoryRunnerFor(new DataMatchesMultipleSuppositionExploration()).multipleBehaviours();

        Assertions.assertThatThrownBy(behaviour.test::run)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has many post-conditions")
                  .hasMessageContaining("2");
    }

    @Test
    void throwsAnExceptionIfMoreThanOneBehaviourIsExplored() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new TwoBehaviourExploration()).tests())
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfNoDataIsGiven() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new NoDataExploration()).tests())
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfDataIsNotIterableOrStream() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new NotIterableDataExploration()).tests())
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfPotentialBehaviourIsNotPredicateOrConsumer() {
        Assertions.assertThatThrownBy(() -> exploratoryRunnerFor(new NotPredicateExploration()).tests())
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void failPerfecytDefinitionIfAtLeastOneDataFailedOnePredicate() {
        PostConditionTest behaviour =
            exploratoryRunnerFor(new AllWrongSuppositionExploration()).perfectDefinition();

        Assertions.assertThatThrownBy(behaviour.test::run)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has failed at least one behaviour")
                  .hasMessageContaining("2")
                  .hasMessageContaining("4");
    }

    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void passPerfecytDefinitionIfNoDataFailedAnyPredicate(Object testInstance) {
        PostConditionTest behaviour =
            exploratoryRunnerFor(testInstance).perfectDefinition();

        try {
            behaviour.test.run();
        } catch (Exception e) {
            fail("Perfect supposition passes perfect definition");
        }
    }

    private static ExploratoryRunner exploratoryRunnerFor(Object testInstance) {
        return ExploratoryRunner.exploratoryRunnerFor(testInstance, IGNORE_PASSING_CASES_CONSUMER);
    }

}
