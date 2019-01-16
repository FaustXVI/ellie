package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;

import java.util.Collection;

@FunctionalInterface
public interface TestResult {
    Collection<ExplorationArguments> argumentsThat(ConditionOutput output);
}
