package com.github.ellie.core.asserters;

import com.github.ellie.core.Exploration;
import com.github.ellie.core.ExplorationResults;
import com.github.ellie.core.TestResult;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.Exploration.exploration;
import static org.assertj.core.api.Assertions.assertThat;

public class UnkownBehaviourTester implements Tester {
    private Tester otherTester;

    public UnkownBehaviourTester(Tester otherTester) {
        this.otherTester = otherTester;
    }

    @Override
    public Stream<Exploration> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(otherTester.tests(results, resultConsumer), Stream.of(dataWithUnknownBehaviour(results, resultConsumer)));
    }

    private Exploration dataWithUnknownBehaviour(ExplorationResults results,
                                                 BiConsumer<String, TestResult> resultConsumer) {
        return exploration("Unknown post-exploration",
                                 () -> {
                                     TestResult result =
                                         results.dataThatPostConditions(b -> b.noneMatch(r -> r == PASS));
                                     resultConsumer.accept("Unknown post-exploration", result);
                                     assertThat(result
                                                       .passingData())
                                         .as("At least one data has unknown post-exploration")
                                         .isEmpty();
                                 });
    }

}
