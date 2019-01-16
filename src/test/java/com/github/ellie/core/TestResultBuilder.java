package com.github.ellie.core;

import com.github.ellie.core.explorers.TestResult;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
        Map<ConditionOutput, List<ExplorationArguments>> map = results.entrySet().stream().collect(
                Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList()))
        );
        return output -> map.getOrDefault(output, Collections.emptyList());
    }
}
