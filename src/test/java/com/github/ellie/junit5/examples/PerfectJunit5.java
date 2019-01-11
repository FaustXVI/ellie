package com.github.ellie.junit5.examples;

import com.github.ellie.junit5.ExploratoryTest;
import com.github.ellie.junit5.annotations.DataProvider;
import com.github.ellie.junit5.annotations.PostCondition;
import com.github.ellie.junit5.annotations.TestedBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class PerfectJunit5 implements ExploratoryTest {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(int n) {
        return n * 2;
    }

    @PostCondition
    public Predicate<Integer> isGreater(int n) {
        return i -> i > n;
    }
}
