package com.github.ellie.core;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface DataAnalyzer {
    List<ExplorationArguments> dataThatBehaviours(
        Predicate<Stream<ConditionOutput>> postConditionPredicate);
}
