package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.Explorer.explore;

public class RunnerBuilder {
    public static Stream<ConditionTest> generateTestsFor(Object testInstance,
                                                         BiConsumer<String, TestResult>
                                                                 passingCases) {
        final InstanceParser instanceParser = new InstanceParser(testInstance);
        ExplorationResults results = explore(instanceParser.data(), instanceParser.executablePostConditions());
        return new MultipleBehaviourRunner(
                new UnkownBehaviourRunner(
                        new ExploratoryRunner(results, passingCases)
                )
        ).tests();
    }
}
