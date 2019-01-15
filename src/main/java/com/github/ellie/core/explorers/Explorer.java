package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.Name;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Explorer {

    Stream<Exploration> explore(PostConditionResults results);

    interface PostConditionResults {
        Map<Name, TestResult> resultByPostConditions();

        TestResult dataThatPostConditions(
                Predicate<Stream<ConditionOutput>> postConditionPredicate);
    }
}
