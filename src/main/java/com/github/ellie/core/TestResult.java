package com.github.ellie.core;

import java.util.Collection;

import static com.github.ellie.core.ConditionOutput.*;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

public class TestResult {
    private final Collection<ExplorationArguments> passes;
    private final Collection<ExplorationArguments> failing;
    private final Collection<ExplorationArguments> ignored;

    TestResult(Collection<? extends ExecutedCondition> testResults) {
        this.passes = selectOutput(testResults, PASS);
        this.failing = selectOutput(testResults, FAIL);
        this.ignored = selectOutput(testResults, IGNORED);
    }

    private Collection<ExplorationArguments> selectOutput(Collection<? extends ExecutedCondition> testResults, ConditionOutput output) {
        return unmodifiableCollection(testResults.stream().filter(e -> e.output == output)
                .map(e -> e.arguments).collect(toList()));
    }

    public Collection<ExplorationArguments> passingData() {
        return passes;
    }

    public Collection<ExplorationArguments> failingData() {
        return failing;
    }

    public Collection<ExplorationArguments> ignoredData() {
        return ignored;
    }
}
