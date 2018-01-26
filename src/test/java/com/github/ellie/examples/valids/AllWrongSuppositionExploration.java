package com.github.ellie.examples.valids;

import com.github.ellie.DataProvider;
import com.github.ellie.ExploratoryTest;
import com.github.ellie.PotentialBehaviour;
import com.github.ellie.TestedBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class AllWrongSuppositionExploration implements ExploratoryTest {

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
