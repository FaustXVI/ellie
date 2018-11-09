package com.github.ellie.core;

import com.github.ellie.examples.valids.OneSuppositionExploration;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerBuilderShould {
    static final BiConsumer<String, TestResult> IGNORE_RESULTS_CONSUMER = (l, o) -> {
    };

    @Test
    void addAllDecorators() {
        Stream<ConditionTest> tests =
            RunnerBuilder.generateTestsFor(new OneSuppositionExploration(), IGNORE_RESULTS_CONSUMER);

        assertThat(tests).hasSize(3);
    }
}
