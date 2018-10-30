package com.github.ellie.core;

@FunctionalInterface
interface BehaviourExecutable {
    ConditionOutput test(Object input);
}
