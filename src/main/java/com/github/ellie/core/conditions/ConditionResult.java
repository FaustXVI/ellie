package com.github.ellie.core.conditions;

import com.github.ellie.core.ExplorationArguments;

public class ConditionResult {

    public final ConditionOutput output;
    public final ExplorationArguments arguments;

    public ConditionResult(ConditionOutput output, ExplorationArguments arguments) {
        this.output = output;
        this.arguments = arguments;
    }
}
