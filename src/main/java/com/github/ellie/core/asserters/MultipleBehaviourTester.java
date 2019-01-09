package com.github.ellie.core.asserters;

import com.github.ellie.core.*;

import java.util.Collection;
import java.util.Optional;
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
    public Stream<Exploration> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(tester.tests(results, resultConsumer),
                dataThatPassesMultiplePostConditions(results, resultConsumer));
    }

    private Stream<Exploration> dataThatPassesMultiplePostConditions(ExplorationResults results,
                                                                     BiConsumer<String, TestResult> resultConsumer) {
        return Stream.of(Exploration.exploration(new Name("Match multiple post-conditions"), () -> {
            TestResult testResult = dataThatPassesMultipleBehaviours(results);
            resultConsumer.accept("Match multiple post-conditions", testResult);
            Collection<ExplorationArguments> dataWithMultipleBehaviours = testResult.passingData();
            if (dataWithMultipleBehaviours.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(new ErrorMessage("At least one data has many post-conditions", dataWithMultipleBehaviours));
            }
        }));
    }

    private TestResult dataThatPassesMultipleBehaviours(ExplorationResults results) {
        return results.dataThatPostConditions(c -> c.filter(r -> r == PASS)
                .count() > 1);
    }

}
