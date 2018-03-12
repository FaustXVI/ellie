package com.github.ellie.core;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PostCondition;
import com.github.ellie.api.TestedBehaviour;
import com.github.ellie.examples.invalids.NoDataExploration;
import com.github.ellie.examples.invalids.NotIterableDataExploration;
import com.github.ellie.examples.invalids.NotPredicateExploration;
import com.github.ellie.examples.invalids.TwoBehaviourExploration;
import com.github.ellie.examples.valids.AllAllowedTypesExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionExploration;
import com.github.ellie.examples.valids.AllWrongSuppositionWithAssumtionsExploration;
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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ExplorerShould {

    @Test
    void giveTestResultForBehaviourBehaviour() {
        OneSuppositionExploration testInstance = new OneSuppositionExploration();
        Explorer explorer = new Explorer(testInstance);
        TestResult testResult = explorer.resultByBehaviour()
                                        .get("times2_is4");
        assertThat(testResult.passingData())
            .extracting(ExplorationArguments::get)
            .containsOnly(args(2));
        assertThat(testResult.failingData())
            .extracting(ExplorationArguments::get)
            .containsOnly(args(4));
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
    void haveNoDataThatPassesNothingWhenPerfectSupposition(Object testInstance) {
        Explorer explorer = new Explorer(testInstance);
        assertThat(explorer.dataThatPassNothing()).isEmpty();
    }


    @ParameterizedTest
    @MethodSource("perfectExplorations")
    void groupResultsByBehaviour(Object testInstance) {
        Explorer explorer = new Explorer(testInstance);
        Map<String, TestResult> dataByBehaviour = explorer.resultByBehaviour();
        assertThat(dataByBehaviour).isNotEmpty();
        assertThat(dataByBehaviour.values()).allMatch(c -> !c.passingData()
                                                             .isEmpty());
    }

    @Test
    void haveUnknownBehaviourContainingExamplesThatPassesNothing() {
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
                  .hasMessageContaining(PostCondition.class.getSimpleName());
    }

    static Stream<Arguments> allWrong() {
        return Stream.of(
            Arguments.of(new AllWrongSuppositionExploration()),
            Arguments.of(new AllWrongSuppositionWithConsumersExploration())
        );
    }

    @ParameterizedTest
    @MethodSource({"allWrong","wrongAssumptions"})
    void givesEmptyPassingDataIfNonePassesPredicate(Object testInstance) {
        try {
            Explorer explorer = new Explorer(testInstance);
            Collection<TestResult> testResults = explorer.resultByBehaviour()
                                                         .values();
            assertThat(testResults)
                .extracting(TestResult::passingData)
                .allMatch(Collection::isEmpty);
        } catch (Exception e) {
            fail("No exception should bubble up");
        }
    }

    @ParameterizedTest
    @MethodSource("allWrong")
    void givesNonEmptyFailingDataIfNonePassesPredicate(Object testInstance) {
        try {
            Explorer explorer = new Explorer(testInstance);
            Collection<TestResult> testResults = explorer.resultByBehaviour()
                                                         .values();
            assertThat(testResults)
                .extracting(TestResult::failingData)
                .allMatch(c -> !c.isEmpty());
        } catch (Exception e) {
            fail("No exception should bubble up");
        }
    }

    static Stream<Arguments> wrongAssumptions() {
        return Stream.of(
            Arguments.of(new AllWrongSuppositionWithAssumtionsExploration())
        );
    }

    @ParameterizedTest
    @MethodSource("wrongAssumptions")
    void givesNonEmptyIgnoredDataIfNonePassesAssumptions(Object testInstance) {
        try {
            Explorer explorer = new Explorer(testInstance);
            Collection<TestResult> testResults = explorer.resultByBehaviour()
                                                         .values();
            assertThat(testResults)
                .extracting(TestResult::ignoredData)
                .allMatch(c -> !c.isEmpty());
        } catch (Exception e) {
            fail("No exception should bubble up");
        }
    }

    @Test
    void givesDataThatPassesMultiplePredicates() {
        Explorer explorer = new Explorer(new DataMatchesMultipleSuppositionExploration());
        assertThat(explorer.dataThatPassesMultipleBehaviours()).isNotEmpty();
    }

}
