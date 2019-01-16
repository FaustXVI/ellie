package com.github.ellie.core;

import com.github.ellie.core.explorers.TestResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapTestResult implements TestResult{
    final Map<ConditionOutput, List<ExplorationArguments>> results;

    public MapTestResult(Map<ConditionOutput, List<ExplorationArguments>> results) {
        this.results = results;
    }

    @Override
    public Collection<ExplorationArguments> argumentsThat(ConditionOutput output) {
        return results.getOrDefault(output, Collections.emptyList());
    }
}
