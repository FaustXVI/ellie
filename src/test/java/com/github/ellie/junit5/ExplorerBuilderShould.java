package com.github.ellie.junit5;

import com.github.ellie.core.explorers.Exploration;
import com.github.ellie.examples.valids.OneSuppositionExploration;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ExplorerBuilderShould {

    @Test
    void addAllDecorators() {
        Stream<Exploration> tests =
            ExplorerBuilder.generateTestsFor(new OneSuppositionExploration());

        assertThat(tests).hasSize(3);
    }
}
