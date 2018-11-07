package com.github.ellie.core;

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
    public Stream<ConditionTest> tests(ExplorationResults results) {
        return Stream.concat(otherRunner.tests(results), Stream.of(dataWithUnknownBehaviour(results)));
    }

    private ConditionTest dataWithUnknownBehaviour(ExplorationResults results) {
        return postConditionTest("Unknown post-condition",
                                 assertThat(results.dataThatBehaviours(b -> b.noneMatch(r -> r == PASS)))
                                     .as("At least one data has unknown post-condition")::isEmpty);
    }

}
