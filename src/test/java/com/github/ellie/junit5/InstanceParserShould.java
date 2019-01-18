package com.github.ellie.junit5;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.conditions.PostConditions;
import com.github.ellie.core.conditions.PreConditions;
import com.github.ellie.core.explorers.Explorer;
import com.github.ellie.examples.invalids.NoDataExploration;
import com.github.ellie.examples.invalids.NotIterableDataExploration;
import com.github.ellie.examples.invalids.NotPredicateExploration;
import com.github.ellie.examples.invalids.TwoBehaviourExploration;
import com.github.ellie.examples.valids.*;
import com.github.ellie.junit5.annotations.DataProvider;
import com.github.ellie.junit5.annotations.PostCondition;
import com.github.ellie.junit5.annotations.TestedBehaviour;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class InstanceParserShould {


    @Test
    void throwsAnExceptionIfNoDataIsGiven() {
        InstanceParser instanceParser = new InstanceParser(new NoDataExploration());
        Assertions.assertThatThrownBy(instanceParser::data)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("no data found")
                .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfDataIsNotIterableOrStream() {
        InstanceParser instanceParser = new InstanceParser(new NotIterableDataExploration());
        Assertions.assertThatThrownBy(instanceParser::data)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("should be iterable or stream")
                .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfMoreThanOneBehaviourIsExplored() {
        InstanceParser instanceParser = new InstanceParser(new TwoBehaviourExploration());
        Assertions.assertThatThrownBy(instanceParser::executablePostConditions)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("only one method")
                .hasMessageContaining(TestedBehaviour.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfPotentialBehaviourIsNotPredicateOrConsumer() {
        InstanceParser instanceParser = new InstanceParser(new NotPredicateExploration());
        Assertions.assertThatThrownBy(instanceParser::executablePostConditions)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("should be predicate or consumer")
                .hasMessageContaining(PostCondition.class.getSimpleName());
    }

    static Stream<Arguments> methodNames() {
        return Stream.of(
                Arguments.of(new OneSuppositionExploration(), List.of("times2_is4")),
                Arguments.of(new TwoSuppositionExploration(), List.of("times3_is6", "times3_is16")),
                Arguments.of(new AllAllowedTypesExploration(), List.of("add_isLessThan10", "add_isMoreThan10"))
        );
    }

    @ParameterizedTest
    @MethodSource("methodNames")
    void namePostconditionConcatenatingBehaviourMethodWithConditionMethod(Object testInstance,
                                                                          List<String> behaviourNames) {
        final InstanceParser instanceParser = new InstanceParser(testInstance);

        assertThat(namesOf(instanceParser.executablePostConditions().explore(instanceParser.data())))
                .containsAll(behaviourNames);
    }

    private Collection<String> namesOf(Explorer.ConditionResults results) {
        return results
                .resultByName()
                .keySet().stream().map(n->n.value)
                .collect(Collectors.toList());
    }

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(new TwoDataProviderExploration(), List.of(args(2), args(4))),
                Arguments.of(new AllAllowedTypesExploration(), List.of(args(2, 4), args(20, 40)))
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void combinesAllDataProviderMethods(Object testInstance, Collection<Object[]> arguments) {
        InstanceParser instanceParser = new InstanceParser(testInstance);
        List<ExplorationArguments> data = instanceParser.data();

        Assertions.assertThat(data)
                .hasSize(2)
                .extracting(ExplorationArguments::get)
                .containsAll(arguments);

    }

    @Test
    void composeBehaviourMethodWithPostConditionMethod() {
        OneSuppositionExploration testInstance = Mockito.spy(new OneSuppositionExploration());
        InstanceParser instanceParser = new InstanceParser(testInstance);
        PostConditions postConditions = instanceParser.executablePostConditions();
        int testedInput = 2;

        postConditions.explore(List.of(ExplorationArguments.of(testedInput)));

        verify(testInstance).times2(testedInput);
        verify(testInstance).is4(testedInput);
    }

    @Test
    void namePreconditionWithConditionMethodName() {
        InstanceParser instanceParser = new InstanceParser(new OneSuppositionOnePreConditionExploration());

        assertThat(namesOf(instanceParser.executablePreConditions().explore(instanceParser.data())))
                .containsExactly("is2");
    }

    @Test
    void callPreConditionMethod() {
        OneSuppositionOnePreConditionExploration testInstance = Mockito.spy(new OneSuppositionOnePreConditionExploration());
        InstanceParser instanceParser = new InstanceParser(testInstance);
        PreConditions preConditions = instanceParser.executablePreConditions();
        int testedInput = 2;

        preConditions.explore(List.of(ExplorationArguments.of(testedInput)));

        verify(testInstance).is2(testedInput);
    }

    private static Object[] args(Object... args) {
        return args;
    }

}