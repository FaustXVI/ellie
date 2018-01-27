package com.github.ellie.examples.invalids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.TestedBehaviour;

import java.util.Collection;
import java.util.List;

public class TwoBehaviourExploration {

    @DataProvider
    public Collection<Integer> numbers() {
        return List.of(2, 4);
    }

    @TestedBehaviour
    public int times2(Integer n) {
        return n * 2;
    }

    @TestedBehaviour
    public int times3(Integer n) {
        return n * 3;
    }
}
