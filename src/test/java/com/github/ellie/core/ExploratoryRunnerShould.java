package com.github.ellie.core;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;
import com.github.ellie.examples.invalids.NoDataExploration;
import com.github.ellie.examples.invalids.NotIterableDataExploration;
import com.github.ellie.examples.invalids.NotPredicateExploration;
import com.github.ellie.examples.invalids.TwoBehaviourExploration;
import com.github.ellie.examples.valids.AllAllowedTypesExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionWithConsumersExploration;
import com.github.ellie.examples.valids.MultipleArgumentsExploration;
import com.github.ellie.examples.valids.OneSuppositionExploration;
import com.github.ellie.examples.valids.PerfectSuppositionExploration;
import com.github.ellie.examples.valids.ProtectedMethodsExploration;
import com.github.ellie.examples.valids.TwoDataProviderExploration;
import com.github.ellie.examples.valids.TwoSuppositionExploration;
import com.github.ellie.examples.valids.ZeroSuppositionExploration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ExploratoryRunnerShould {

    private static final BiConsumer<String, Iterable<Object[]>> IGNORE_PASSING_CASES_CONSUMER = (l, o) -> {
    };

    static Stream<Arguments> numberOfBehaviours() {
        return Stream.of(
            Arguments.of(new ZeroSuppositionExploration(), 0),
            Arguments.of(new OneSuppositionExploration(), 1),
            Arguments.of(new TwoSuppositionExploration(), 2),
            Arguments.of(new MultipleArgumentsExploration(), 1)
        );
    }

    @ParameterizedTest
    @MethodSource("numberOfBehaviours")
    void createsOneNodePerPotentialBehaviorAndOneMoreForUndefinedBehaviour(Object testInstance,
                                                                           int numberOfBehaviours) {
        Stream<? extends DynamicNode> behaviours = generateTestsFor(testInstance);
        assertThat(behaviours).hasSize(numberOfBehaviours + 1);
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
        Stream<? extends DynamicNode> behaviours = generateTestsFor(testInstance);

        assertThat(behaviours).extracting(DynamicNode::getDisplayName)
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
        Stream<? extends DynamicNode> behaviours = generateTestsFor(testInstance);

        assertThat(behaviours).extracting(DynamicNode::getDisplayName)
                              .last()
                              .isEqualTo("Unknown behaviour");
    }


    @Test
    void testsEachBehavioursWithEachData() {
        OneSuppositionExploration testInstance = Mockito.spy(new OneSuppositionExploration());
        generateTestsFor(testInstance)
            .forEach(t -> {
                try {
                    t.getExecutable()
                     .execute();
                } catch (Throwable throwable) {
                    //test result is not the concern here
                }
            });
        verify(testInstance, atLeastOnce()).numbers();
        verify(testInstance, times(2)).times2(2);
        verify(testInstance, times(2)).times2(4);
        verify(testInstance, times(2)).is4(2);
        verify(testInstance, times(2)).is4(4);
    }

    @Test
    void combinesAllDataProviderMethods() {
        TwoDataProviderExploration testInstance = Mockito.spy(new TwoDataProviderExploration());
        generateTestsFor(testInstance)
            .forEach(t -> {
                try {
                    t.getExecutable()
                     .execute();
                } catch (Throwable throwable) {
                    //test result is not the concern here
                }
            });
        verify(testInstance, atLeastOnce()).two();
        verify(testInstance, atLeastOnce()).four();
        verify(testInstance, times(2)).times2(2);
        verify(testInstance, times(2)).times2(4);
        verify(testInstance, times(2)).is4(2);
        verify(testInstance, times(2)).is4(4);
    }


    @Test
    void callsConsumerWithDataThatPassesPotentialBehaviour() {
        Map<String, Iterable<Object[]>> dataThatPass = new HashMap<>();
        OneSuppositionExploration testInstance = new OneSuppositionExploration();

        ExploratoryRunner.generateTestsFor(testInstance, dataThatPass::put)
                         .forEach(t -> {
                             try {
                                 t.getExecutable()
                                  .execute();
                             } catch (Throwable throwable) {
                                 //test result is not the concern here
                             }
                         });

        String passingSupposition = "times2_is4";
        assertThat(dataThatPass).containsOnlyKeys(passingSupposition);
        assertThat(dataThatPass.get(passingSupposition)).containsExactly(new Object[]{2});
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
        Stream<DynamicTest> behaviours = generateTestsFor(testInstance);

        Assertions.assertThatThrownBy(firstTestOf(behaviours)::execute)
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
        Stream<DynamicTest> behaviours =
            generateTestsFor(testInstance);

        try {
            unknowBehaviourTestOf(behaviours).execute();
        } catch (Throwable throwable) {
            fail("All behaviour are found, no unknown behaviour should be left", throwable);
        }
    }

    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void passAllTestsIfPerfectComprehension(Object testInstance) {
        List<DynamicTest> behaviours =
            generateTestsFor(testInstance).collect(Collectors.toList());

        try {
            for (DynamicTest behaviour : behaviours) {
                behaviour.getExecutable()
                         .execute();
            }
        } catch (Throwable throwable) {
            fail("Perfect comprehension should be green", throwable);
        }
    }

    @Test
    void failUnknownBehaviourIfAtLeastOneDataValidatesAnyPredicateAndLogIt() {
        Stream<DynamicTest> behaviours =
            generateTestsFor(new AllWrongSuppositionExploration());

        Assertions.assertThatThrownBy(unknowBehaviourTestOf(behaviours)::execute)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has unknown behaviour")
                  .hasMessageContaining("2")
                  .hasMessageContaining("4");
    }

    @Test
    void throwsAnExceptionIfMoreThanOneBehaviourIsExplored() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new TwoBehaviourExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("only one method")
                  .hasMessageContaining(TestedBehaviour.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfNoDataIsGiven() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new NoDataExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("no data found")
                  .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfDataIsNotIterableOrStream() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new NotIterableDataExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("should be iterable or stream")
                  .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfPotentialBehaviourIsNotPredicateOrConsumer() {
        Assertions.assertThatThrownBy(() -> generateTestsFor(new NotPredicateExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("should be predicate or consumer")
                  .hasMessageContaining(PotentialBehaviour.class.getSimpleName());
    }

    private Executable firstTestOf(Stream<DynamicTest> behaviours) {
        return behaviours.findFirst()
                         .orElseThrow(() -> new AssertionError("Should have at least one behaviour but got none"))
                         .getExecutable();
    }

    private Executable unknowBehaviourTestOf(Stream<DynamicTest> behaviours) {
        return behaviours.collect(Collectors.toCollection(LinkedList::new))
                         .getLast()
                         .getExecutable();
    }

    private static Stream<DynamicTest> generateTestsFor(Object testInstance) {
        return ExploratoryRunner.generateTestsFor(testInstance, IGNORE_PASSING_CASES_CONSUMER);
    }

}
