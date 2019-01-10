package com.github.ellie.core.asserters;

import com.github.ellie.core.Exploration;
import com.github.ellie.core.PostConditionResults;
import com.github.ellie.core.TestResult;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface Tester {


    Stream<Exploration> tests(PostConditionResults results, BiConsumer<String, TestResult> resultConsumer);
}
