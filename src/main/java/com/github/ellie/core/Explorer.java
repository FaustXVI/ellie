package com.github.ellie.core;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

class Explorer {

    static ExplorationResults explore(List<ExplorationArguments> data, List<ExecutableCondition> postConditions) {
        Map<ExplorationArguments, Map<String, ConditionOutput>> dataToPostConditionsResults = data.stream()
                .collect(toMap(identity(),
                        d -> testPostConditions(d, postConditions)));
        return new ExplorationResults(dataToPostConditionsResults);
    }

    private static Map<String, ConditionOutput> testPostConditions(ExplorationArguments d,
                                                                   List<ExecutableCondition> postConditions) {
        return postConditions.stream()
                .collect(toMap(ExecutableCondition::name, b -> b.testWith(d)));
    }

}
