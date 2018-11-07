package com.github.ellie.core;

import java.util.List;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;

public class MultipleBehaviourRunner implements Runner {

    private final Runner runner;

    public MultipleBehaviourRunner(Runner runner) {
        this.runner = runner;
    }

    @Override
    public Stream<ConditionTest> tests(ExplorationResults results) {
        return Stream.concat(runner.tests(results),
                             dataThatPassesMultiplePostConditions(results));
    }

    private Stream<ConditionTest> dataThatPassesMultiplePostConditions(ExplorationResults results) {
        return Stream.of(ConditionTest.postConditionTest("Match multiple post-conditions",                                  assertThat(dataThatPassesMultipleBehaviours(results))
            .as("At least one data has many post-conditions")::isEmpty));
    }

    private List<ExplorationArguments> dataThatPassesMultipleBehaviours(ExplorationResults results) {
        return results.dataThatBehaviours(c -> c.filter(r -> r == PASS)
                                        .count() > 1);
    }

}
