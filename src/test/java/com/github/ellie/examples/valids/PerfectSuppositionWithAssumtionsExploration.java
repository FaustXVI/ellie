package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PostCondition;
import com.github.ellie.api.TestedBehaviour;
import org.junit.jupiter.api.Assumptions;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class PerfectSuppositionWithAssumtionsExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(-2, 4);
    }

    @TestedBehaviour
    public int times2(int n) {
        return n * 2;
    }

    @PostCondition
    public Predicate<Integer> isGreater(int n) {
        Assumptions.assumeTrue(n > 0);
        return i -> i > n;
    }

    @PostCondition
    public Predicate<Integer> isLesser(int n) {
        Assumptions.assumeTrue(n < 0);
        return i -> i < n;
    }
}
