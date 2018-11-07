package com.github.ellie.core;

import java.util.stream.Stream;

public interface Runner {
    Stream<ConditionTest> tests(ExplorationResults results);
}
