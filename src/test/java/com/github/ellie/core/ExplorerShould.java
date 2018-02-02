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
import com.github.ellie.examples.valids.DataMatchesMultipleSuppositionExploration;
import com.github.ellie.examples.valids.OneSuppositionExploration;
import com.github.ellie.examples.valids.PerfectSuppositionExploration;
import com.github.ellie.examples.valids.ProtectedMethodsExploration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ExplorerShould {

    @Test
    void givesDataThatPassGivenBehaviour() {
        OneSuppositionExploration testInstance = new OneSuppositionExploration();
        Explorer explorer = new Explorer(testInstance);
        assertThat(explorer.dataThatPasses("times2_is4"))
            .extracting(ExplorationArguments::get)
            .containsOnly(args(2));
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
    void unknownBehaviourIsEmptyWhenPerfectSupposition(Object testInstance) {
        Explorer explorer = new Explorer(testInstance);
        assertThat(explorer.dataThatPassNothing()).isEmpty();
    }


    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void groupsDataByBehaviour(Object testInstance) {
        Explorer explorer = new Explorer(testInstance);
        explorer.behavioursTo(explorer::dataThatPasses)
                .forEach(r -> assertThat(r).isNotEmpty());
    }

    @Test
    void unknownBehaviourValidatesNoPredicate() {
        Explorer explorer = new Explorer(new AllWrongSuppositionExploration());

        assertThat(explorer.dataThatPassNothing())
            .extracting(ExplorationArguments::get)
            .contains(args(2), args(4));
    }

    private Object[] args(Object... ns) {
        return ns;
    }

    @Test
    void throwsAnExceptionIfMoreThanOneBehaviourIsExplored() {
        Assertions.assertThatThrownBy(() -> new Explorer(new TwoBehaviourExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("only one method")
                  .hasMessageContaining(TestedBehaviour.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfNoDataIsGiven() {
        Assertions.assertThatThrownBy(() -> new Explorer(new NoDataExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("no data found")
                  .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfDataIsNotIterableOrStream() {
        Assertions.assertThatThrownBy(() -> new Explorer(new NotIterableDataExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("should be iterable or stream")
                  .hasMessageContaining(DataProvider.class.getSimpleName());
    }

    @Test
    void throwsAnExceptionIfPotentialBehaviourIsNotPredicateOrConsumer() {
        Assertions.assertThatThrownBy(() -> new Explorer(new NotPredicateExploration()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("should be predicate or consumer")
                  .hasMessageContaining(PotentialBehaviour.class.getSimpleName());
    }

    static Stream<Arguments> allWrong() {
        return Stream.of(
            Arguments.of(new AllWrongSuppositionExploration()),
            Arguments.of(new AllWrongSuppositionWithConsumersExploration())
        );
    }

    @ParameterizedTest
    @MethodSource("allWrong")
    void givesEmptyDataIfNonePassesPredicate(Object testInstance) {
        Explorer explorer = new Explorer(testInstance);
        explorer.behavioursTo(explorer::dataThatPasses)
                .forEach(r -> assertThat(r).isEmpty());
    }

    @Test
    void givesDataThatPassesMultiplePredicates() {
        Explorer explorer = new Explorer(new DataMatchesMultipleSuppositionExploration());
        assertThat(explorer.dataThatPassesMultipleBehaviours()).isNotEmpty();
    }

}
