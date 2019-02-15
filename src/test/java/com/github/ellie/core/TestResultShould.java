package com.github.ellie.core;

import com.github.ellie.core.explorers.TestResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

public class TestResultShould {

    public static final ExplorationArguments ONE = ExplorationArguments.of(1);
    public static final ExplorationArguments TWO = ExplorationArguments.of(2);
    public static final ExplorationArguments THREE = ExplorationArguments.of(3);
    public static final ExplorationArguments FOUR = ExplorationArguments.of(4);

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> ONE_TWO = new HashMap<>() {{
        put(PASS, List.of(ONE, TWO));
        put(FAIL, List.of(THREE, FOUR));
    }};

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> THREE_FOUR = new HashMap<>() {{
        put(PASS, List.of(THREE, FOUR));
        put(FAIL, List.of(ONE, TWO));
    }};

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> ONE_THREE = new HashMap<>() {{
        put(PASS, List.of(ONE, THREE));
        put(FAIL, List.of(TWO, FOUR));
    }};

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> THREE_QUARTERS = new HashMap<>() {{
        put(PASS, List.of(ONE, TWO, THREE));
        put(FAIL, List.of(FOUR));
    }};

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> ALL_PASS = new HashMap<>() {{
        put(PASS, List.of(ONE, TWO, THREE, FOUR));
    }};

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> ALL_FAIL = new HashMap<>() {{
        put(FAIL, List.of(ONE, TWO, THREE, FOUR));
    }};

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> NOT_SAME_SIZE_OF_ARGUMENTS = new HashMap<>() {{
        put(PASS, List.of(ONE, TWO));
        put(FAIL, List.of(FOUR));
    }};

    private static Stream<Arguments> correlations() {
        return Stream.of(
                Arguments.of(ONE_TWO, ONE_TWO, 1d),
                Arguments.of(ONE_TWO, ONE_THREE, 0d),
                Arguments.of(ONE_TWO, THREE_QUARTERS, 1d / Math.sqrt(3d)),
                Arguments.of(ONE_TWO, NOT_SAME_SIZE_OF_ARGUMENTS, 0d),
                Arguments.of(ONE_TWO, THREE_FOUR, 1d)
        );
    }


    private static Stream<Arguments> noCorrelations() {
        return Stream.of(
                Arguments.of(ONE_TWO, ALL_PASS),
                Arguments.of(ALL_PASS, ONE_TWO),
                Arguments.of(ALL_FAIL, ONE_TWO),
                Arguments.of(ALL_PASS, THREE_QUARTERS),
                Arguments.of(ALL_FAIL, ALL_PASS)
        );
    }

    @ParameterizedTest
    @MethodSource("correlations")
    void computeCorrelationBetweenTwoResults(Map<ConditionOutput, Collection<ExplorationArguments>> firstExploration,
                                             Map<ConditionOutput, Collection<ExplorationArguments>> secondExploration, double correlation) {
        TestResult firstResult = o -> firstExploration.getOrDefault(o, List.of());
        TestResult secondResult = o -> secondExploration.getOrDefault(o, List.of());

        assertThat(firstResult.computeCorrelationFactorWith(secondResult))
                .isNotNaN()
                .isCloseTo(correlation, withPercentage(0.1));

    }

    @ParameterizedTest
    @MethodSource("noCorrelations")
    void isNaNWhenNoCorrelation(Map<ConditionOutput, Collection<ExplorationArguments>> firstExploration,
                                             Map<ConditionOutput, Collection<ExplorationArguments>> secondExploration) {
        TestResult firstResult = o -> firstExploration.getOrDefault(o, List.of());
        TestResult secondResult = o -> secondExploration.getOrDefault(o, List.of());

        assertThat(firstResult.computeCorrelationFactorWith(secondResult))
                .isNaN();

    }

    @ParameterizedTest
    @MethodSource("correlations")
    void beSymetrical(Map<ConditionOutput, Collection<ExplorationArguments>> firstExploration,
                                             Map<ConditionOutput, Collection<ExplorationArguments>> secondExploration, double correlation) {
        TestResult firstResult = o -> firstExploration.getOrDefault(o, List.of());
        TestResult secondResult = o -> secondExploration.getOrDefault(o, List.of());

        double leftToRight = firstResult.computeCorrelationFactorWith(secondResult);
        double rightToLeft = secondResult.computeCorrelationFactorWith(firstResult);
        assertThat(leftToRight)
                .isCloseTo(rightToLeft, withPercentage(0.1));

    }

}
