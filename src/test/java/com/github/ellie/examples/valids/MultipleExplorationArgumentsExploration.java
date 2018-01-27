package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.junit5.ExploratoryTest;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class MultipleExplorationArgumentsExploration implements ExploratoryTest {

    @DataProvider
    public Collection<ExplorationArguments> numbers() {
        return List.of(ExplorationArguments.of(2, 3));
    }

    @TestedBehaviour
    public int times2(int a, int b) {
        return a * b;
    }

    @PotentialBehaviour
    public Predicate<Integer> isGreater(int a, int b) {
        return i -> i == 0;
    }
}
