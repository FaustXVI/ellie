package com.github.ellie.core.asserters;

import com.github.ellie.core.ExplorableCondition;
import com.github.ellie.core.Exploration;
import com.github.ellie.core.ExplorationResults;
import com.github.ellie.core.TestResult;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.ExplorableCondition.*;
import static org.assertj.core.api.Assertions.assertThat;

public class MultipleBehaviourTester implements Tester {

    private final Tester tester;

    public MultipleBehaviourTester(Tester tester) {
        this.tester = tester;
    }

    @Override
    public Stream<Exploration> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(tester.tests(results, resultConsumer),
                             dataThatPassesMultiplePostConditions(results, resultConsumer));
    }

    private Stream<Exploration> dataThatPassesMultiplePostConditions(ExplorationResults results,
                                                                     BiConsumer<String, TestResult> resultConsumer) {
        return Stream.of(Exploration.exploration(new Name("Match multiple post-conditions"), () -> {
            TestResult testResult = dataThatPassesMultipleBehaviours(results);
            resultConsumer.accept("Match multiple post-conditions", testResult);
            assertThat(
                testResult.passingData())
                .as("At least one data has many post-conditions")
                .isEmpty();
        }));
    }

    private TestResult dataThatPassesMultipleBehaviours(ExplorationResults results) {
        return results.dataThatPostConditions(c -> c.filter(r -> r == PASS)
                                                .count() > 1);
    }

}
