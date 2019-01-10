package com.github.ellie.junit5;

import com.github.ellie.core.Exploration;
import com.github.ellie.examples.valids.OneSuppositionExploration;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TesterBuilderShould {

    @Test
    void addAllDecorators() {
        Stream<Exploration> tests =
            RunnerBuilder.generateTestsFor(new OneSuppositionExploration());

        assertThat(tests).hasSize(3);
    }
}
