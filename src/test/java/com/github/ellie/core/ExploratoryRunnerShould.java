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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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

    private static final BiConsumer<String, Collection<ExplorationArguments>> IGNORE_PASSING_CASES_CONSUMER = (l, o) -> {
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
    void createsOneNodePerPotentialBehaviorAndOneMoreForUndefinedBehaviourAndMultiple(Object testInstance,
                                                                           int numberOfBehaviours) {
        Stream<BehaviourTest> behaviours = generateTestsFor(testInstance);
        assertThat(behaviours).hasSize(numberOfBehaviours + 2);
    }


    static Stream<Arguments> methodNames() {
        return Stream.of(
            Arguments.of(new OneSuppositionExploration(), "times2", List.of("is4")),
            Arguments.of(new TwoSuppositionExploration(), "times3", List.of("is6", "is16"))
        );
    }

    @ParameterizedTest
    @MethodSource("methodNames")
    void namesNodesWithActionAndSupposedBehaviour(Object testInstance, String actionName,
                                                  List<String> behaviourNames) {
        Stream<BehaviourTest> behaviours = generateTestsFor(testInstance);

        assertThat(behaviours).extracting(b->b.name)
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
    void addsUnknownBehaviourLast(Object testInstance) {
        Stream<BehaviourTest> behaviours = generateTestsFor(testInstance);

        assertThat(behaviours).extracting(b->b.name)
                              .last()
                              .isEqualTo("Unknown behaviour");
    }


    @ParameterizedTest
    @MethodSource("testInstances")
    void addsMultipleBehaviourSecondLast(Object testInstance) {
        List<BehaviourTest> behaviours = generateTestsFor(testInstance).collect(Collectors.toList());

        assertThat(behaviours).extracting(b->b.name)
                              .element(behaviours.size() - 2)
                              .isEqualTo("Match multiple behaviours");
    }

    @Test
    void testsEachBehavioursWithEachData() {
        OneSuppositionExploration testInstance = Mockito.spy(new OneSuppositionExploration());
        generateTestsFor(testInstance)
            .forEach(t -> {
                try {
                    t.test
                     .run();
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
        generateTestsFor(testInstance)
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
    void callsConsumerWithDataThatPassesPotentialBehaviour() {
        Map<String, Iterable<ExplorationArguments>> dataThatPass = new HashMap<>();
        OneSuppositionExploration testInstance = new OneSuppositionExploration();

        ExploratoryRunner.generateTestsFor(testInstance, dataThatPass::put)
                         .forEach(t -> {
                             try {
                                 t.test
                                  .run();
                             } catch (Throwable throwable) {
                                 //test result is not the concern here
                             }
                         });

        String passingSupposition = "times2_is4";
        assertThat(dataThatPass).containsOnlyKeys(passingSupposition);
        assertThat(dataThatPass.get(passingSupposition))
            .extracting(ExplorationArguments::get)
            .containsExactly(new Object[]{2});
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
        Stream<BehaviourTest> behaviours = generateTestsFor(testInstance);

        Assertions.assertThatThrownBy(firstTestOf(behaviours)::run)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("no data validates this behaviour");
    }


    static Stream<Arguments> perfectExplorations() {
        return Stream.of(
            Arguments.of(new PerfectSuppositionExploration()),
            Arguments.of(new AllAllowedTypesExploration()),
            Arguments.of(new ProtectedMethodsExploration())
        );
    }

    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void passUnknownBehaviourIfAllDataValidatesAtLeastPredicate(Object testInstance) {
        Stream<BehaviourTest> behaviours =
            generateTestsFor(testInstance);

        try {
            unknownBehaviourTestOf(behaviours).run();
        } catch (Throwable throwable) {
            fail("All behaviour are found, no unknown behaviour should be left", throwable);
        }
    }

    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void passAllTestsIfPerfectComprehension(Object testInstance) {
        List<BehaviourTest> behaviours =
            generateTestsFor(testInstance).collect(Collectors.toList());

        try {
            for (BehaviourTest behaviour : behaviours) {
                behaviour.test.run();
            }
        } catch (Throwable throwable) {
            fail("Perfect comprehension should be green", throwable);
        }
    }

    @Test
    void failUnknownBehaviourIfAtLeastOneDataValidatesAnyPredicateAndLogIt() {
        Stream<BehaviourTest> behaviours =
            generateTestsFor(new AllWrongSuppositionExploration());

        Assertions.assertThatThrownBy(unknownBehaviourTestOf(behaviours)::run)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has unknown behaviour")
                  .hasMessageContaining("2")
                  .hasMessageContaining("4");
    }

    @Test
    void failMultipleBehaviourIfAtLeastOneDataValidatesMultiplePredicateAndLogIt() {
        Stream<BehaviourTest> behaviours =
            generateTestsFor(new DataMatchesMultipleSuppositionExploration());

        Assertions.assertThatThrownBy(multipleBehaviourTestOf(behaviours)::run)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has many behaviours")
                  .hasMessageContaining("2");
    }

    @Test
    void throwsAnExceptionIfMoreThanOneBehaviourIsExplored() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new TwoBehaviourExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfNoDataIsGiven() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new NoDataExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfDataIsNotIterableOrStream() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new NotIterableDataExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    @Test
    void throwsAnExceptionIfPotentialBehaviourIsNotPredicateOrConsumer() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new NotPredicateExploration()))
                  .isInstanceOf(AssertionError.class);
    }

    private Runnable firstTestOf(Stream<BehaviourTest> behaviours) {
        return behaviours.findFirst()
                         .orElseThrow(() -> new AssertionError("Should have at least one behaviour but got none"))
                         .test;
    }

    private Runnable unknownBehaviourTestOf(Stream<BehaviourTest> behaviours) {
        return behaviours.collect(Collectors.toCollection(LinkedList::new))
                         .getLast()
                         .test;
    }

    private Runnable multipleBehaviourTestOf(Stream<BehaviourTest> behaviours) {
        List<BehaviourTest> bs = behaviours.collect(Collectors.toList());
        return bs.get(bs.size() - 2).test;
    }

    private static Stream<BehaviourTest> generateTestsFor(Object testInstance) {
        return ExploratoryRunner.generateTestsFor(testInstance, IGNORE_PASSING_CASES_CONSUMER);
    }

}
