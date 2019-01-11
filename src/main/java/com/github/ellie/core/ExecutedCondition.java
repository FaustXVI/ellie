package com.github.ellie.core;

public class ExecutedCondition {

    public final Name name;
    public final ConditionOutput output;
    public final ExplorationArguments arguments;

    public ExecutedCondition(Name name, ConditionOutput output, ExplorationArguments arguments) {
        this.name = name;
        this.output = output;
        this.arguments = arguments;
    }
}
