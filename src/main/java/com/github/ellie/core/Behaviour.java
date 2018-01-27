package com.github.ellie.core;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;

interface Behaviour extends BiFunction<Object, ExplorationArguments, Predicate<Object>> {
    static Behaviour noneOf(Collection<? extends Behaviour> predicates) {
        return (Object testInstance, ExplorationArguments explorationArguments) -> (Object value) ->
            predicates.stream()
                      .noneMatch(f -> f.apply(testInstance, explorationArguments)
                                       .test(value));
    }
}
