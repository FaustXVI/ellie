package com.github.ellie.examples.valids;

import com.github.ellie.DataProvider;
import com.github.ellie.ExploratoryTest;
import com.github.ellie.TestedBehaviour;

import java.util.Collection;
import java.util.List;

public class ZeroSuppositionExploration implements ExploratoryTest {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(Integer n) {
        return n * 2;
    }

}
