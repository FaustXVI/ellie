package com.github.ellie.examples.valids;

import com.github.ellie.DataProvider;
import com.github.ellie.ExploratoryTest;
import com.github.ellie.PotentialBehaviour;
import com.github.ellie.TestedBehaviour;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class MultipleArgumentsExploration implements ExploratoryTest {

    @DataProvider
    public Collection<Arguments> numbers() {
        return List.of(Arguments.of(2, 3));
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
