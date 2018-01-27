package com.github.ellie.core;

import org.junit.jupiter.params.provider.Arguments;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;

interface Behaviour extends BiFunction<Object, Arguments, Predicate<Object>> {
    static Behaviour noneOf(Collection<? extends Behaviour> predicates) {
        return (Object testInstance, Arguments arguments) -> (Object value) ->
            predicates.stream()
                      .noneMatch(f -> f.apply(testInstance, arguments)
                                       .test(value));
    }
}
