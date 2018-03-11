package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PostCondition;
import com.github.ellie.api.TestedBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class DataMatchesMultipleSuppositionExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times3(Integer n) {
        return n * 3;
    }

    @PostCondition
    public Predicate<Integer> is6(int n) {
        return i -> i == 6;
    }

    @PostCondition
    public Predicate<Integer> isMoreThanOriginal(int n) {
        return i -> i > n;
    }

}
