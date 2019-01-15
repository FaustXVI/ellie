package com.github.ellie.core.conditions;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;

public interface NamedCondition extends Condition{
    NamedConditionResult testWith(ExplorationArguments explorationArguments);

    Name name();

}
