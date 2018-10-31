package com.github.ellie.core;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

class Explorer implements DataAnalyzer {

    private final ExplorationResults explorationResults;

    Explorer(InstanceParser instanceParser) {
        List<ExecutableCondition> postConditions = instanceParser.executablePostConditions();
        List<ExplorationArguments> data = instanceParser.data();
        explorationResults = explore(postConditions, data);
    }

    private static ExplorationResults explore(List<ExecutableCondition> postConditions,
                                              List<ExplorationArguments> data) {
        Map<ExplorationArguments, Map<String, ConditionOutput>> dataToPostConditionsResults = data.stream()
                                                                                                  .collect(
                                                                                                      toMap(identity(),
                                                                                                            d -> testPostConditions(
                                                                                                                d,
                                                                                                                postConditions)));
        return new ExplorationResults(dataToPostConditionsResults);
    }

    private static Map<String, ConditionOutput> testPostConditions(ExplorationArguments d,
                                                                   List<ExecutableCondition> postConditions) {
        return postConditions.stream()
                             .collect(toMap(ExecutableCondition::name, b -> b.testWith(d)));
    }

    Map<String, TestResult> resultByBehaviour() {
        return explorationResults.resultByBehaviour();
    }

    @Override
    public List<ExplorationArguments> dataThatBehaviours(
        Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        return explorationResults.dataThatBehaviours(postConditionPredicate);
    }

}
