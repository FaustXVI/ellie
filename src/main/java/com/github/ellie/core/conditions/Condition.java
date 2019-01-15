package com.github.ellie.core.conditions;

import com.github.ellie.core.ExplorationArguments;

public interface Condition {
    ConditionResult testWith(ExplorationArguments explorationArguments);

}
