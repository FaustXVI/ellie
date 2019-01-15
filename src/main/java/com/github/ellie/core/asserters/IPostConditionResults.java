package com.github.ellie.core.asserters;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.Name;
import com.github.ellie.core.asserters.TestResult;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface IPostConditionResults {
    Map<Name, TestResult> resultByPostConditions();

    TestResult dataThatPostConditions(
            Predicate<Stream<ConditionOutput>> postConditionPredicate);
}
