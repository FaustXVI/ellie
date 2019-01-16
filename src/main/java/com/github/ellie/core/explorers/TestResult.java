package com.github.ellie.core.explorers;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.conditions.ConditionResult;

import java.util.Collection;

import static com.github.ellie.core.ConditionOutput.*;
import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

// TODO replace with interface ? argumentsGeneratingOutput(ConditionOutput)
public class TestResult {
    private final Collection<ExplorationArguments> passes;
    private final Collection<ExplorationArguments> failing;

    public TestResult(Collection<? extends ConditionResult> testResults) {
        this.passes = selectOutput(testResults, PASS);
        this.failing = selectOutput(testResults, FAIL);
    }

    private Collection<ExplorationArguments> selectOutput(Collection<? extends ConditionResult> testResults, ConditionOutput output) {
        return unmodifiableCollection(testResults.stream().filter(e -> e.output == output)
                .map(e -> e.arguments).collect(toList()));
    }

    public Collection<ExplorationArguments> passingData() {
        return passes;
    }

    public Collection<ExplorationArguments> failingData() {
        return failing;
    }

}
