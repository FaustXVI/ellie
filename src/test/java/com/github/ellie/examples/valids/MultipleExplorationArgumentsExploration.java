package com.github.ellie.examples.valids;

import com.github.ellie.junit5.annotations.DataProvider;
import com.github.ellie.junit5.annotations.PostCondition;
import com.github.ellie.junit5.annotations.TestedBehaviour;
import com.github.ellie.core.ExplorationArguments;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class MultipleExplorationArgumentsExploration {

    @DataProvider
    public Collection<ExplorationArguments> numbers() {
        return List.of(ExplorationArguments.of(2, 3));
    }

    @TestedBehaviour
    public int times2(int a, int b) {
        return a * b;
    }

    @PostCondition
    public Predicate<Integer> isGreater(int a, int b) {
        return i -> i == 0;
    }
}
