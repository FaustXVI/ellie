package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.junit5.ExploratoryTest;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ProtectedMethodsExploration implements ExploratoryTest {


    @DataProvider
    Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    int times2(int n) {
        return n * 2;
    }

    @PotentialBehaviour
    Predicate<Integer> isGreater(int n) {
        return i -> i > n;
    }

}
