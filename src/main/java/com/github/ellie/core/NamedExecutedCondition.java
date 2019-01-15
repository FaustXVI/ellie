package com.github.ellie.core;

class NamedExecutedCondition extends ExecutedCondition {

    final Name name;

    NamedExecutedCondition(Name name, ConditionOutput output, ExplorationArguments arguments) {
        super(output, arguments);
        this.name = name;
    }
}
