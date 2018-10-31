package com.github.ellie.core;

import com.github.ellie.examples.valids.OneSuppositionExploration;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerBuilderShould {
    @Test
    void addAllDecorators() {
        Stream<ConditionTest> tests =
            RunnerBuilder.generateTestsFor(new OneSuppositionExploration(), (a, b) -> {
            });

        assertThat(tests).hasSize(3);
    }
}
