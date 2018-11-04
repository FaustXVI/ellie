package com.github.ellie.examples;

import com.github.ellie.api.PostCondition;
import com.github.ellie.api.TestedBehaviour;
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
