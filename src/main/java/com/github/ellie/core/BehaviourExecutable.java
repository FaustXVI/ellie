package com.github.ellie.core;

@FunctionalInterface
interface BehaviourExecutable {
    PostConditionOutput test(Object input);
}
