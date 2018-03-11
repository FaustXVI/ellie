package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PostCondition;
import com.github.ellie.api.TestedBehaviour;
import org.assertj.core.api.Assertions;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class AllWrongSuppositionWithConsumersExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(int n) {
        return n * 2;
    }

    @PostCondition
    public Consumer<Integer> is0(int n) {
        return i -> Assertions.assertThat(i).isEqualTo(0);
    }
}
