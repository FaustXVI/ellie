package com.github.ellie.junit5;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import com.github.ellie.core.explorers.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class ExplorerBuilder {
    static Stream<Exploration> generateTestsFor(Object testInstance) {
        InstanceParser instanceParser = new InstanceParser(testInstance);
        // TODO check only one call of data
        List<ExplorationArguments> data = instanceParser.data();
        Explorer.PostConditionResults results = instanceParser.executablePostConditions().explore(data);
        Explorer.PreConditionResults preconditionResults = instanceParser.executablePreConditions().explore(data);
        return new MultipleBehaviourExplorer(
                new UnkownBehaviourExplorer(
                        new AtLeastOneMatchExplorer()
                )
        ).explore(results,preconditionResults);
    }

    private static Explorer.PreConditionResults emptyPreconditions() {
        return new Explorer.PreConditionResults() {
            @Override
            public Map<Name, TestResult> resultByName() {
                return Map.of();
            }

            @Override
            public Map<ExplorationArguments, List<ConditionOutput>> outputByArgument() {
                return Map.of();
            }
        };
    }
}
