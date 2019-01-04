package com.github.ellie.junit5;

import com.github.ellie.core.*;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.Explorer.explore;

class RunnerBuilder {
    static Stream<ConditionTest> generateTestsFor(Object testInstance, BiConsumer<String, TestResult> resultConsumer) {
        InstanceParser instanceParser = new InstanceParser(testInstance);
        ExplorationResults results = explore(instanceParser.data(), instanceParser.executablePostConditions());
        return new MultipleBehaviourTester(
                new UnkownBehaviourTester(
                        new ExploratoryTester()
                )
        ).tests(results,resultConsumer);
    }
}
