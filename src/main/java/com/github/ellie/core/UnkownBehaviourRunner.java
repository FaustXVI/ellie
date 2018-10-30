package com.github.ellie.core;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.ConditionTest.postConditionTest;
import static org.assertj.core.api.Assertions.assertThat;

public class UnkownBehaviourRunner implements Runner {
    private Runner otherRunner;

    public UnkownBehaviourRunner(Runner otherRunner) {
        this.otherRunner = otherRunner;
    }

    @Override
    public Stream<ConditionTest> tests() {
        return Stream.concat(otherRunner.tests(), Stream.of(dataWithUnknownBehaviour()));
    }

    private ConditionTest dataWithUnknownBehaviour() {
        return postConditionTest("Unknown post-condition",
                                 assertThat(dataThatBehaviours(b -> b.noneMatch(r -> r == PASS)))
                                     .as("At least one data has unknown post-condition")::isEmpty);
    }

    @Override
    public List<ExplorationArguments> dataThatBehaviours(Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        return otherRunner.dataThatBehaviours(postConditionPredicate);
    }
}
