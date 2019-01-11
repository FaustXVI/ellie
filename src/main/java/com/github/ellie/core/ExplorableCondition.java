package com.github.ellie.core;

public interface ExplorableCondition {
    ConditionOutput testWith(ExplorationArguments explorationArguments);

    Name name();

}
