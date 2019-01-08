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

import static com.github.ellie.core.ConditionOutput.fromPredicate;
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
        Function<List<ConditionOutput>, ConditionOutput> outputFunction
                = fromPredicate(l -> postConditionPredicate.test(l.stream()));
        return new TestResult(flipMap(mapDataTo(outputFunction)));
    }

    private Map<ExplorationArguments, ConditionOutput> mapDataTo(Function<List<ConditionOutput>, ConditionOutput> outputFunction) {
        return explorations.stream()
                .collect(
                        groupingBy(e -> e.arguments,
                                collectingAndThen(
                                        mapping(e -> e.output, toList()), outputFunction)));
    }

    private <K, V> Map<V, List<K>> flipMap(Map<K, V> argumentsResults) {
        return argumentsResults.entrySet()
                .stream()
                .collect(groupingBy(Map.Entry::getValue,
                        mapping(Map.Entry::getKey, toList())));
    }

}
