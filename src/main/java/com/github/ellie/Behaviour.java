package com.github.ellie;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;

interface Behaviour extends BiFunction<Object, Object[], Predicate<Object>> {
    static Behaviour noneOf(Collection<? extends Behaviour> predicates) {
        return (Object testInstance, Object[] arguments) -> (Object value) ->
            predicates.stream()
                      .noneMatch(f -> f.apply(testInstance, arguments)
                                       .test(value));
    }
}
