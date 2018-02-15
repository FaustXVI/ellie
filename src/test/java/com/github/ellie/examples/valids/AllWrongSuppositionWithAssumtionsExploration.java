package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;
import org.junit.jupiter.api.Assumptions;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class AllWrongSuppositionWithAssumtionsExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(int n) {
        return n * 2;
    }

    @PotentialBehaviour
    public Consumer<Integer> is0(int n) {
        return i -> Assumptions.assumeTrue(n < 2);
    }
}
