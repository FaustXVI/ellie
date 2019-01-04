package com.github.ellie.core;

public class ExecutedExploration {

    public final ExplorableCondition.Name name;
    public final ConditionOutput output;
    public final ExplorationArguments arguments;

    public ExecutedExploration(ExplorableCondition.Name name, ConditionOutput output, ExplorationArguments arguments) {
        this.name = name;
        this.output = output;
        this.arguments = arguments;
    }
}
