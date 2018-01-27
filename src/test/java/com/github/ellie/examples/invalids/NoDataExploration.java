package com.github.ellie.examples.invalids;

import com.github.ellie.api.TestedBehaviour;

public class NoDataExploration {

    @TestedBehaviour
    public int times3(Integer n) {
        return n * 3;
    }
}
