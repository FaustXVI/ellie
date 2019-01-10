package com.github.ellie.core.asserters;

import com.github.ellie.core.*;
import com.github.ellie.core.ExplorableCondition.Name;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.Exploration.exploration;

public class UnkownBehaviourTester implements Tester {
    private Tester otherTester;

    public UnkownBehaviourTester(Tester otherTester) {
        this.otherTester = otherTester;
    }

    @Override
    public Stream<Exploration> tests(PostConditionResults results, BiConsumer<String, TestResult> resultConsumer) {
        return Stream.concat(otherTester.tests(results, resultConsumer), Stream.of(dataWithUnknownBehaviour(results, resultConsumer)));
    }

    private Exploration dataWithUnknownBehaviour(PostConditionResults results,
                                                 BiConsumer<String, TestResult> resultConsumer) {
        return exploration(new Name("Unknown post-exploration"),
                () -> {
                    TestResult result =
                            results.dataThatPostConditions(b -> b.anyMatch(r -> r == PASS));
                    resultConsumer.accept("Unknown post-exploration", result);
                    Collection<ExplorationArguments> dataWithUnknownBehaviour = result.failingData();
                    if (dataWithUnknownBehaviour.isEmpty()) {
                        return Optional.empty();
                    } else {
                        return Optional.of(new ErrorMessage("At least one data has unknown post-exploration", dataWithUnknownBehaviour));
                    }
                });
    }

}
