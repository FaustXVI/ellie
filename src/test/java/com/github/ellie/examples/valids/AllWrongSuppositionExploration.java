package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class AllWrongSuppositionExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(int n) {
        return n * 2;
    }

    @PotentialBehaviour
    public Predicate<Integer> is0(int n) {
        return i -> i == 0;
    }
}
