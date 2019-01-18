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

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> ONE_THREE = new HashMap<>() {{
        put(PASS, List.of(ONE, THREE));
        put(FAIL, List.of(TWO, FOUR));
    }};

    public static final Map<ConditionOutput, Collection<ExplorationArguments>> THREE_QUARTERS = new HashMap<>() {{
        put(PASS, List.of(ONE, TWO, THREE));
        put(FAIL, List.of(FOUR));
    }};

    private static Stream<Arguments> correlations() {
        return Stream.of(
                Arguments.of(ONE_TWO, ONE_TWO, 1d),
                Arguments.of(ONE_TWO, ONE_THREE, 0d),
                Arguments.of(ONE_TWO, THREE_QUARTERS, 1d / Math.sqrt(3d))
        );
    }

    @ParameterizedTest
    @MethodSource("correlations")
    void computeCorrelationBetweenTwoResults(Map<ConditionOutput, Collection<ExplorationArguments>> firstExploration,
                                             Map<ConditionOutput, Collection<ExplorationArguments>> secondExploration, double correlation) {
        TestResult firstResult = firstExploration::get;
        TestResult secondResult = secondExploration::get;

        assertThat(firstResult.computeCorrelationFactorWith(secondResult))
                .isCloseTo(correlation, withPercentage(0.1));

    }
}
