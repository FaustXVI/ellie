package com.github.ellie;

import com.github.ellie.examples.invalids.NoDataExploration;
import com.github.ellie.examples.invalids.NotIterableDataExploration;
import com.github.ellie.examples.invalids.NotPredicateExploration;
import com.github.ellie.examples.invalids.TwoBehaviourExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionExploration;
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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ExploratoryRunnerShould {


    // Can generate easily input methods

    // Nice to have
    // PotentialBehaviour => returns a consumer (can use assertions)
    // Warning if a same input goes in to different behaviours
    // framework independant


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
        Stream<? extends DynamicNode> behaviours = ExploratoryRunner.generateTestsFor(testInstance);
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
        Stream<? extends DynamicNode> behaviours = ExploratoryRunner.generateTestsFor(testInstance);

        assertThat(behaviours).extracting(DynamicNode::getDisplayName)
                              .containsAll(
                                  createName(behaviourNames, behaviourName -> actionName + " " + behaviourName))
        ;
    }

    private List<String> createName(List<String> behaviourNames, Function<String, String> behaviourNameConstructor) {
        return behaviourNames.stream()
                             .map(behaviourNameConstructor)
                             .collect(Collectors.toList());
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
        Stream<? extends DynamicNode> behaviours = ExploratoryRunner.generateTestsFor(testInstance);

        assertThat(behaviours).extracting(DynamicNode::getDisplayName)
                              .last()
                              .isEqualTo("Unknown behaviour");
    }


    @Test
    void testsEachBehavioursWithEachData() {
        OneSuppositionExploration testInstance = Mockito.spy(new OneSuppositionExploration());
        ExploratoryRunner.generateTestsFor(testInstance)
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
        ExploratoryRunner.generateTestsFor(testInstance)
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
    void failPotentialBehaviourIfNotDataValidatesPredicate() {
        Stream<DynamicTest> behaviours =
            ExploratoryRunner.generateTestsFor(new AllWrongSuppositionExploration());

        Assertions.assertThatThrownBy(firstTestOf(behaviours)::execute)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("no data validates this behaviour");
    }


    static Stream<Arguments> perfectExplorations() {
        return Stream.of(
            Arguments.of(new PerfectSuppositionExploration()),
            Arguments.of(new ProtectedMethodsExploration())
        );
    }

    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void passUnknownBehaviourIfAllDataValidatesAtLeastPredicate(Object testInstance) {
        Stream<DynamicTest> behaviours =
            ExploratoryRunner.generateTestsFor(testInstance);

        try {
            unknowBehaviourTestOf(behaviours).execute();
        } catch (Throwable throwable) {
            fail("All behaviour are found", throwable);
        }
    }

    @Test
    void failUnknownBehaviourIfAtLeastOneDataValidatesAnyPredicateAndLogIt() {
        Stream<DynamicTest> behaviours =
            ExploratoryRunner.generateTestsFor(new AllWrongSuppositionExploration());

        Assertions.assertThatThrownBy(unknowBehaviourTestOf(behaviours)::execute)
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("one data has unknown behaviour")
                  .hasMessageContaining("2")
                  .hasMessageContaining("4");
    }

    @Test
    void throwsAnExceptionIfMoreThanOneBehaviourIsExplored() {
        Assertions.assertThatThrownBy(() -> ExploratoryRunner.generateTestsFor(new TwoBehaviourExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("only one method")
                  .hasMessageContaining(TestedBehaviour.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfNoDataIsGiven() {
        Assertions.assertThatThrownBy(() -> ExploratoryRunner.generateTestsFor(new NoDataExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("no data found")
                  .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfDataIsNotIterableOrStream() {
        Assertions.assertThatThrownBy(() -> ExploratoryRunner.generateTestsFor(new NotIterableDataExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("should be iterable or stream")
                  .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfPotentialBehaviourIsNotPredicate() {
        Assertions.assertThatThrownBy(() -> ExploratoryRunner.generateTestsFor(new NotPredicateExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("should be predicate")
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

}
