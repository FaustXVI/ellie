package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class RunnerBuilder {
    public static Stream<ConditionTest> generateTestsFor(Object testInstance,
                                                         BiConsumer<String, TestResult>
                                                                 passingCases) {
        return new ExploratoryRunner(new Explorer(testInstance), passingCases).tests();
    }
}
