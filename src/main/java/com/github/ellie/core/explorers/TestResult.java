package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.conditions.ConditionResult;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

// TODO replace with interface ? argumentsGeneratingOutput(ConditionOutput)
public class TestResult {
    private Collection<? extends ConditionResult> testResults;

    public TestResult(Collection<? extends ConditionResult> testResults) {
        this.testResults = testResults;
    }

    public Collection<ExplorationArguments> argumentsThat(ConditionOutput output) {
        return unmodifiableCollection(testResults.stream().filter(e -> e.output == output)
                .map(e -> e.arguments).collect(toList()));
    }

}
