package com.github.ellie.core;

public interface ExecutableCondition {
    ConditionOutput testWith(ExplorationArguments explorationArguments);

    String name();
}
