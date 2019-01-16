package com.github.ellie.core.conditions;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.explorers.TestResult;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;
import static java.util.stream.Collectors.toList;

class UnmodifiableTestResult implements TestResult {
    private Collection<? extends ConditionResult> testResults;

    UnmodifiableTestResult(Collection<? extends ConditionResult> testResults) {
        this.testResults = testResults;
    }

    @Override
    public Collection<ExplorationArguments> argumentsThat(ConditionOutput output) {
        return unmodifiableCollection(testResults.stream().filter(e -> e.output == output)
                .map(e -> e.arguments).collect(toList()));
    }

}
