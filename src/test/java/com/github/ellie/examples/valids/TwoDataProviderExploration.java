package com.github.ellie.examples.valids;

import com.github.ellie.DataProvider;
import com.github.ellie.ExploratoryTest;
import com.github.ellie.PotentialBehaviour;
import com.github.ellie.TestedBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TwoDataProviderExploration implements ExploratoryTest {

    @DataProvider
    public Collection<Integer> two() {
        return List.of(2);
    }

    @DataProvider
    public Stream<Integer> four() {
        return Stream.of(4);
    }

    @TestedBehaviour
    public int times2(Integer n) {
        return n * 2;
    }

    @PotentialBehaviour
    public Predicate<Integer> is4(int n) {
        return i -> i == 4;
    }

}
