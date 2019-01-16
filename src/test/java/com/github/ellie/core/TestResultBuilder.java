package com.github.ellie.core;

import com.github.ellie.core.conditions.ConditionResult;
import com.github.ellie.core.explorers.TestResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestResultBuilder {

    private final Map<ExplorationArguments, ConditionOutput> results = new LinkedHashMap<>();

    public static TestResultBuilder aTestResult() {
        return new TestResultBuilder();
    }

    public TestResultBuilder with(ExplorationArguments arguments, ConditionOutput output) {
        results.put(arguments, output);
        return this;
    }

    public TestResult build() {
        return new TestResult(results.entrySet().stream()
                .map(e -> new ConditionResult(e.getValue(), e.getKey())).collect(Collectors.toList()));
    }
}
