package com.github.ellie.examples.valids;

import com.github.ellie.junit5.annotations.PostCondition;
import com.github.ellie.junit5.annotations.TestedBehaviour;
import org.junit.jupiter.api.Assumptions;

import java.util.function.Predicate;

public class AssumeNotNegativeAndTestIsTwo {

    @TestedBehaviour
    public int times2(Integer n) {
        return n;
    }

    @PostCondition
    public Predicate<Integer> is2(int n) {
        return i -> {
            Assumptions.assumeTrue(n > 0);
            return i == 2;
        };
    }

}
