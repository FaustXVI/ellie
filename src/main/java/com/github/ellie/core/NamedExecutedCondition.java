package com.github.ellie.core;

public class NamedExecutedCondition extends ExecutedCondition {

    public final Name name;

    public NamedExecutedCondition(Name name, ConditionOutput output, ExplorationArguments arguments) {
        super(output, arguments);
        this.name = name;
    }
}
