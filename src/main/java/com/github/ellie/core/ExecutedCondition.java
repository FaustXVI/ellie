package com.github.ellie.core;

public class ExecutedCondition {

    public final ConditionOutput output;
    public final ExplorationArguments arguments;

    public ExecutedCondition(ConditionOutput output, ExplorationArguments arguments) {
        this.output = output;
        this.arguments = arguments;
    }
}
