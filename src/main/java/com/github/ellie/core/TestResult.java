package com.github.ellie.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.IGNORED;
import static com.github.ellie.core.ConditionOutput.PASS;

public class TestResult {
    private final Collection<ExplorationArguments> passes;
    private final Collection<ExplorationArguments> failing;
    private final Collection<ExplorationArguments> ignored;

    public TestResult(Map<ConditionOutput,List<ExplorationArguments>> testResults) {
        this.passes = testResults.getOrDefault(PASS, Collections.emptyList());
        this.failing = testResults.getOrDefault(FAIL, Collections.emptyList());
        this.ignored = testResults.getOrDefault(IGNORED,Collections.emptyList());
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
