package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.TestedBehaviour;

import java.util.Collection;
import java.util.List;

public class ZeroSuppositionExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(Integer n) {
        return n * 2;
    }

}
