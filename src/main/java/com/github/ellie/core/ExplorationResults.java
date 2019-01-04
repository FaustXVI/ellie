package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class ExplorationResults {
    private final Collection<ExecutedExploration> explorations;

    public ExplorationResults(Collection<ExecutedExploration> explorations) {
        this.explorations = explorations;
    }


    public Map<Name, TestResult> resultByPostConditions() {
        return explorations.stream()
                .collect(groupingBy(e -> e.name,
                        collectingAndThen(toList(), TestResult::new)));
    }

    public TestResult dataThatPostConditions(
            Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        Map<ExplorationArguments, ConditionOutput> argumentsResults = explorations.stream()
                .collect(
                        groupingBy(e -> e.arguments,
                        applyPredicate(postConditionPredicate)));
        return new TestResult(argumentsResults.entrySet()
                .stream()
                .collect(groupingBy(Map.Entry::getValue,
                        mapping(Map.Entry::getKey, toList()))));
    }

    private static Collector<ExecutedExploration, ?, ConditionOutput> applyPredicate(Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        Function<Stream<ConditionOutput>, ConditionOutput> fromPredicate = ConditionOutput.fromPredicate(postConditionPredicate);
        return mapping(e -> e.output,
                collectingAndThen(toList(), e -> fromPredicate.apply(
                        e.stream()
                )));
    }

}
