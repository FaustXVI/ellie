package com.github.ellie.core.asserters;

import com.github.ellie.core.*;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.ExplorableCondition.Name;

public class MultipleBehaviourTester implements Tester {

    private final Tester tester;

    public MultipleBehaviourTester(Tester tester) {
        this.tester = tester;
    }

    @Override
    public Stream<Exploration> tests(PostConditionResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(tester.tests(results, resultConsumer),
                dataThatPassesMultiplePostConditions(results, resultConsumer));
    }

    private Stream<Exploration> dataThatPassesMultiplePostConditions(PostConditionResults results,
                                                                     BiConsumer<String, TestResult> resultConsumer) {
        return Stream.of(Exploration.exploration(new Name("Match multiple post-conditions"), () -> {
            TestResult testResult = dataThatPassesMaximumOneBehaviour(results);
            resultConsumer.accept("Match multiple post-conditions", testResult);
            Collection<ExplorationArguments> dataWithMultipleBehaviours = testResult.failingData();
            if (dataWithMultipleBehaviours.isEmpty()) {
                return new ExplorationResult(testResult);
            } else {
                return new ExplorationResult(new ErrorMessage("At least one data has many post-conditions", dataWithMultipleBehaviours), testResult);
            }
        }));
    }

    private TestResult dataThatPassesMaximumOneBehaviour(PostConditionResults results) {
        return results.dataThatPostConditions(c -> c.filter(r -> r == PASS)
                .count() <= 1);
    }

}
