package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;

public class MultipleBehaviourTester implements Tester {

    private final Tester tester;

    public MultipleBehaviourTester(Tester tester) {
        this.tester = tester;
    }

    @Override
    public Stream<ConditionTest> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(tester.tests(results, resultConsumer),
                             dataThatPassesMultiplePostConditions(results, resultConsumer));
    }

    private Stream<ConditionTest> dataThatPassesMultiplePostConditions(ExplorationResults results,
                                                                       BiConsumer<String, TestResult> resultConsumer) {
        return Stream.of(ConditionTest.postConditionTest("Match multiple post-conditions", () -> {
            TestResult testResult = dataThatPassesMultipleBehaviours(results);
            resultConsumer.accept("Match multiple post-conditions", testResult);
            assertThat(
                testResult.passingData())
                .as("At least one data has many post-conditions")
                .isEmpty();
        }));
    }

    private TestResult dataThatPassesMultipleBehaviours(ExplorationResults results) {
        return results.dataThatBehaviours(c -> c.filter(r -> r == PASS)
                                                .count() > 1);
    }

}