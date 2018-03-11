package com.github.ellie.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.ellie.core.PostConditionOutput.FAIL;
import static com.github.ellie.core.PostConditionOutput.PASS;

public class TestResult {
    private final Collection<ExplorationArguments> passes;
    private final Collection<ExplorationArguments> failing;

    public TestResult(Map<PostConditionOutput,List<ExplorationArguments>> testResults) {
        this.passes = testResults.getOrDefault(PASS, Collections.emptyList());
        this.failing = testResults.getOrDefault(FAIL, Collections.emptyList());
    }


    public Collection<ExplorationArguments> passingData() {
        return passes;
    }

    public Collection<ExplorationArguments> failingData() {
        return failing;
    }

}
