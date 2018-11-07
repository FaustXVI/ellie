package com.github.ellie.core;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;

class ExplorationResults {
    private final Map<ExplorationArguments, Map<String, ConditionOutput>> dataToPostConditionsResults;

    ExplorationResults(
        Map<ExplorationArguments, Map<String, ConditionOutput>> dataToPostConditionsResults) {
        this.dataToPostConditionsResults = dataToPostConditionsResults;
    }


    Map<String, TestResult> resultByBehaviour() {
        return dataToPostConditionsResults.values()
                                          .stream()
                                          .flatMap(m -> m.keySet()
                                                         .stream())
                                          .distinct()
                                          .collect(toMap(identity(), this::resultsFor));
    }


    private TestResult resultsFor(String behaviourName) {
        return new TestResult(
            dataToPostConditionsResults.entrySet()
                                       .stream()
                                       .collect(groupingBy(e -> e.getValue()
                                                                 .get(behaviourName),
                                                           mapping(Map.Entry::getKey, Collectors.toList()))));
    }


    List<ExplorationArguments> dataThatBehaviours(
        Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        return dataToPostConditionsResults.entrySet()
                                          .stream()
                                          .filter(e -> postConditionPredicate.test(e.getValue()
                                                                                    .values()
                                                                                    .stream()))
                                          .map(Map.Entry::getKey)
                                          .collect(Collectors.toList());
    }

}
