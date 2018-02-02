package com.github.ellie.core;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

interface Behaviour extends Function<ExplorationArguments, Predicate<Object>> {
    static Behaviour noneOf(Collection<? extends Behaviour> predicates) {
        return (ExplorationArguments explorationArguments) -> (Object value) ->
            predicates.stream()
                      .noneMatch(f -> f.apply(explorationArguments)
                                       .test(value));
    }
}
