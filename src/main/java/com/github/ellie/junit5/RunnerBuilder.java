package com.github.ellie.junit5;

import com.github.ellie.core.asserters.*;

import java.util.stream.Stream;

class RunnerBuilder {
    static Stream<Exploration> generateTestsFor(Object testInstance) {
        InstanceParser instanceParser = new InstanceParser(testInstance);
        IPostConditionResults results = instanceParser.executablePostConditions().explore(instanceParser.data());
        return new MultipleBehaviourTester(
                new UnkownBehaviourTester(
                        new ExploratoryTester()
                )
        ).tests(results);
    }
}
