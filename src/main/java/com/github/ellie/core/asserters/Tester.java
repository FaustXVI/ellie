package com.github.ellie.core.asserters;

import com.github.ellie.core.Exploration;
import com.github.ellie.core.ExplorationResults;
import com.github.ellie.core.TestResult;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface Tester {


    Stream<Exploration> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer);
}
