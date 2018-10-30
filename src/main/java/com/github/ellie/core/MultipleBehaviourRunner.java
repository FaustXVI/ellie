package com.github.ellie.core;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;

public class MultipleBehaviourRunner implements Runner {

    private final Runner runner;

    public MultipleBehaviourRunner(Runner runner) {
        this.runner = runner;
    }

    @Override
    public Stream<ConditionTest> tests() {
        return Stream.concat(runner.tests(),
                             dataThatPassesMultiplePostConditions());
    }

    private Stream<ConditionTest> dataThatPassesMultiplePostConditions() {
        return Stream.of(ConditionTest.postConditionTest("Match multiple post-conditions",                                  assertThat(dataThatPassesMultipleBehaviours())
            .as("At least one data has many post-conditions")::isEmpty));
    }

    private List<ExplorationArguments> dataThatPassesMultipleBehaviours() {
        return dataThatBehaviours(c -> c.filter(r -> r == PASS)
                                        .count() > 1);
    }

    @Override
    public List<ExplorationArguments> dataThatBehaviours(
        Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        return runner.dataThatBehaviours(postConditionPredicate);
    }
}
