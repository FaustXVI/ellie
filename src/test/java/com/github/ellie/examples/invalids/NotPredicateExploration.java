package com.github.ellie.examples.invalids;

import com.github.ellie.junit5.annotations.DataProvider;
import com.github.ellie.junit5.annotations.PostCondition;
import com.github.ellie.junit5.annotations.TestedBehaviour;

import java.util.Collection;
import java.util.List;

public class NotPredicateExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(Integer n) {
        return n * 2;
    }

    @PostCondition
    public int times3(Integer n) {
        return n * 3;
    }
}
