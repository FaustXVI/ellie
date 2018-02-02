package com.github.ellie.core;

import java.util.function.Function;
import java.util.function.Predicate;

interface Behaviour extends Function<ExplorationArguments, Predicate<Object>> {
}
