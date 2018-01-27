package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.junit5.ExploratoryTest;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class TwoSuppositionExploration implements ExploratoryTest {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times3(Integer n) {
        return n * 3;
    }

    @PotentialBehaviour
    public Predicate<Integer> is6(int n) {
        return i -> i == 6;
    }

    @PotentialBehaviour
    public Predicate<Integer> is16(int n) {
        return i -> i == 16;
    }

}
