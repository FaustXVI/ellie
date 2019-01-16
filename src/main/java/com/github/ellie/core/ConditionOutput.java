package com.github.ellie.core;

import java.util.function.Function;
import java.util.function.Predicate;

public enum ConditionOutput {
    PASS,
    FAIL;

    public static <T> Function<T, ConditionOutput> fromPredicate(Predicate<T> predicate) {
        return (T t) -> {
            try {
                return predicate.test(t) ? PASS : FAIL;
            } catch (Exception e) {
                return FAIL;
            }
        };
    }
}
