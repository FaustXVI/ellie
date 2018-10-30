package com.github.ellie.core;

import java.util.stream.Stream;

public interface Runner extends DataAnalyzer{
    Stream<ConditionTest> tests();
}
