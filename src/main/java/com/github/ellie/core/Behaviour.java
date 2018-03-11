package com.github.ellie.core;

import java.util.function.Function;

interface Behaviour extends Function<ExplorationArguments, BehaviourExecutable> {
}
