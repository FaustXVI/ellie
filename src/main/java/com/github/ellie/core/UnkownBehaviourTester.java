package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.ConditionTest.postConditionTest;
import static org.assertj.core.api.Assertions.assertThat;

public class UnkownBehaviourTester implements Tester {
    private Tester otherTester;

    public UnkownBehaviourTester(Tester otherTester) {
        this.otherTester = otherTester;
    }

    @Override
    public Stream<ConditionTest> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(otherTester.tests(results, resultConsumer), Stream.of(dataWithUnknownBehaviour(results, resultConsumer)));
    }

    private ConditionTest dataWithUnknownBehaviour(ExplorationResults results,
                                                   BiConsumer<String, TestResult> resultConsumer) {
        return postConditionTest("Unknown post-condition",
                                 () -> {
                                     TestResult result =
                                         results.dataThatBehaviours(b -> b.noneMatch(r -> r == PASS));
                                     resultConsumer.accept("Unknown post-condition", result);
                                     assertThat(result
                                                       .passingData())
                                         .as("At least one data has unknown post-condition")
                                         .isEmpty();
                                 });
    }

}
