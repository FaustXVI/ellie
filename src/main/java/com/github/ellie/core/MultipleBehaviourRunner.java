package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;

public class MultipleBehaviourRunner implements Runner {

    private final Runner runner;

    public MultipleBehaviourRunner(Runner runner) {
        this.runner = runner;
    }

    @Override
    public Stream<ConditionTest> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(runner.tests(results,resultConsumer),
                             dataThatPassesMultiplePostConditions(results));
    }

    private Stream<ConditionTest> dataThatPassesMultiplePostConditions(ExplorationResults results) {
        return Stream.of(ConditionTest.postConditionTest("Match multiple post-conditions",                                  assertThat(dataThatPassesMultipleBehaviours(results).passingData())
            .as("At least one data has many post-conditions")::isEmpty));
    }

    private TestResult dataThatPassesMultipleBehaviours(ExplorationResults results) {
        return results.dataThatBehaviours(c -> c.filter(r -> r == PASS)
                                        .count() > 1);
    }

}
