package com.github.ellie.core.conditions;

import org.opentest4j.TestAbortedException;

import java.util.function.Function;
import java.util.function.Predicate;

public enum ConditionOutput {
    PASS,
    IGNORED,
    FAIL;

    public static <T> Function<T, ConditionOutput> fromPredicate(Predicate<T> predicate) {
        return (T t) -> {
            try {
                return predicate.test(t) ? PASS : FAIL;
            } catch (TestAbortedException e) {
                return IGNORED;
            }
        };
    }
}
