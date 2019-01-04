package com.github.ellie.core;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ExplorationResults {
    private final Map<ExplorationArguments, Map<String, ConditionOutput>> dataToPostConditionsResults;

    ExplorationResults(
        Map<ExplorationArguments, Map<String, ConditionOutput>> dataToPostConditionsResults) {
        this.dataToPostConditionsResults = dataToPostConditionsResults;
    }


    public Map<String, TestResult> resultByPostConditions() {
        return postConditionsNames()
                                          .collect(toMap(identity(), this::resultsFor));
    }

    private Stream<String> postConditionsNames() {
        return dataToPostConditionsResults.values()
                                          .stream()
                                          .flatMap(m -> m.keySet()
                                                         .stream())
                                          .distinct();
    }


    private TestResult resultsFor(String postConditionName) {
        return testResultsWhere(e -> e.getValue()
                                      .get(postConditionName));
    }

    public TestResult dataThatPostConditions(
            Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        return testResultsWhere(e ->
                                    ConditionOutput.fromPredicate(postConditionPredicate)
                                                   .apply(e.getValue()
                                                           .values()
                                                           .stream()));
    }

    private TestResult testResultsWhere(
        Function<Map.Entry<ExplorationArguments, Map<String, ConditionOutput>>, ConditionOutput> conditionOutputFunction) {
        return new TestResult(
            dataToPostConditionsResults
                .entrySet()
                .stream()
                .collect(groupingBy(
                    conditionOutputFunction,
                    mapping(Map.Entry::getKey, toList()))));
    }

}
