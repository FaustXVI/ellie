package com.github.ellie.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.IGNORED;
import static com.github.ellie.core.ConditionOutput.PASS;
import static java.util.Collections.unmodifiableCollection;

public class TestResult {
    private final Collection<ExplorationArguments> passes;
    private final Collection<ExplorationArguments> failing;
    private final Collection<ExplorationArguments> ignored;

    public TestResult(Map<ConditionOutput,List<ExplorationArguments>> testResults) {
        this.passes = unmodifiableCollection(testResults.getOrDefault(PASS, Collections.emptyList()));
        this.failing = unmodifiableCollection(testResults.getOrDefault(FAIL, Collections.emptyList()));
        this.ignored = unmodifiableCollection(testResults.getOrDefault(IGNORED,Collections.emptyList()));
    }

    public TestResult(List<ExecutedExploration> testResults) {
        this(testResults.stream()
                .collect(Collectors.groupingBy(e -> e.output, Collectors.mapping(e -> e.arguments, Collectors.toList()))));
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
