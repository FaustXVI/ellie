package com.github.ellie.core.asserters;

import com.github.ellie.core.*;
import com.github.ellie.core.Name;

import java.util.Collection;
import java.util.stream.Stream;

import static com.github.ellie.core.conditions.ConditionOutput.PASS;
import static com.github.ellie.core.asserters.Exploration.exploration;

public class UnkownBehaviourTester implements Tester {
    private Tester otherTester;

    public UnkownBehaviourTester(Tester otherTester) {
        this.otherTester = otherTester;
    }

    @Override
    public Stream<Exploration> tests(PostConditionResults results) {
        return Stream.concat(otherTester.tests(results), Stream.of(dataWithUnknownBehaviour(results)));
    }

    private Exploration dataWithUnknownBehaviour(PostConditionResults results) {
        return exploration(new Name("Unknown post-exploration"),
                (errorMessageHandler) -> {
                    TestResult result =
                            results.dataThatPostConditions(b -> b.anyMatch(r -> r == PASS));
                    Collection<ExplorationArguments> dataWithUnknownBehaviour = result.failingData();
                    if (!dataWithUnknownBehaviour.isEmpty()) {
                        ErrorMessage errorMessage = new ErrorMessage("At least one data has unknown post-exploration", dataWithUnknownBehaviour);
                        errorMessageHandler.accept(errorMessage);
                    }
                    return result;
                });
    }

}
