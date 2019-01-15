package com.github.ellie.junit5;

import com.github.ellie.core.explorers.*;

import java.util.stream.Stream;

class RunnerBuilder {
    static Stream<Exploration> generateTestsFor(Object testInstance) {
        InstanceParser instanceParser = new InstanceParser(testInstance);
        Explorer.PostConditionResults results = instanceParser.executablePostConditions().explore(instanceParser.data());
        return new MultipleBehaviourExplorer(
                new UnkownBehaviourExplorer(
                        new AtLeastOneMatchExplorer()
                )
        ).explore(results);
    }
}
