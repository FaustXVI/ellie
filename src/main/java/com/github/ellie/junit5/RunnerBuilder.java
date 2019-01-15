package com.github.ellie.junit5;

import com.github.ellie.core.Exploration;
import com.github.ellie.core.PostConditionResults;
import com.github.ellie.core.asserters.ExploratoryTester;
import com.github.ellie.core.asserters.MultipleBehaviourTester;
import com.github.ellie.core.asserters.UnkownBehaviourTester;

import java.util.stream.Stream;

class RunnerBuilder {
    static Stream<Exploration> generateTestsFor(Object testInstance) {
        InstanceParser instanceParser = new InstanceParser(testInstance);
        PostConditionResults results = instanceParser.executablePostConditions().explore(instanceParser.data());
        return new MultipleBehaviourTester(
                new UnkownBehaviourTester(
                        new ExploratoryTester()
                )
        ).tests(results);
    }
}
