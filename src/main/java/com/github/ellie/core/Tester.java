package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface Tester {


    Stream<ConditionTest> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer);
}
