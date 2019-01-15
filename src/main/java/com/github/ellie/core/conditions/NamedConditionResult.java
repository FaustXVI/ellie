package com.github.ellie.core.conditions;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;

public class NamedConditionResult extends ConditionResult {

    public final Name name;

    public NamedConditionResult(Name name, ConditionOutput output, ExplorationArguments arguments) {
        super(output, arguments);
        this.name = name;
    }
}
