package com.github.ellie.junit5;

import com.github.ellie.core.ConditionTest;
import com.github.ellie.core.ExploratoryTesterShould;
import com.github.ellie.examples.valids.OneSuppositionExploration;
import com.github.ellie.junit5.RunnerBuilder;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TesterBuilderShould {

    @Test
    void addAllDecorators() {
        Stream<ConditionTest> tests =
            RunnerBuilder.generateTestsFor(new OneSuppositionExploration(), ExploratoryTesterShould.IGNORE_RESULTS_CONSUMER);

        assertThat(tests).hasSize(3);
    }
}
