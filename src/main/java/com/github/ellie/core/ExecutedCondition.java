package com.github.ellie.core;

class ExecutedCondition {

    final ConditionOutput output;
    final ExplorationArguments arguments;

    ExecutedCondition(ConditionOutput output, ExplorationArguments arguments) {
        this.output = output;
        this.arguments = arguments;
    }
}
