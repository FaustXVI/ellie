package com.github.ellie.core;

public interface Condition {
    ConditionOutput testWith(ExplorationArguments explorationArguments);

    Name name();

}
