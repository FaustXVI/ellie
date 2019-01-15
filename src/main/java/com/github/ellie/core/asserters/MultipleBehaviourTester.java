package com.github.ellie.core.asserters;

import com.github.ellie.core.*;

import java.util.Collection;
import java.util.stream.Stream;

import static com.github.ellie.core.conditions.ConditionOutput.PASS;

import com.github.ellie.core.Name;

public class MultipleBehaviourTester implements Tester {

    private final Tester tester;

    public MultipleBehaviourTester(Tester tester) {
        this.tester = tester;
    }

    @Override
    public Stream<Exploration> tests(PostConditionResults results) {
        return Stream.concat(tester.tests(results),
                dataThatPassesMultiplePostConditions(results));
    }

    private Stream<Exploration> dataThatPassesMultiplePostConditions(PostConditionResults results) {
        return Stream.of(Exploration.exploration(new Name("Match multiple post-conditions"),
                (errorMessageHandler) -> {
                    TestResult testResult = dataThatPassesMaximumOneBehaviour(results);
                    Collection<ExplorationArguments> dataWithMultipleBehaviours = testResult.failingData();
                    if (!dataWithMultipleBehaviours.isEmpty()) {
                        ErrorMessage errorMessage = new ErrorMessage("At least one data has many post-conditions", dataWithMultipleBehaviours);
                        errorMessageHandler.accept(errorMessage);
                    }
                    return testResult;
                }));
    }

    private TestResult dataThatPassesMaximumOneBehaviour(PostConditionResults results) {
        return results.dataThatPostConditions(c -> c.filter(r -> r == PASS)
                .count() <= 1);
    }

}
