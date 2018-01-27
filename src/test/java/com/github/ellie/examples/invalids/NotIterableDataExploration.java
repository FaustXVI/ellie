package com.github.ellie.examples.invalids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.TestedBehaviour;

public class NotIterableDataExploration {


    @DataProvider
    public Integer numbers() {
        return 2;
    }

    @TestedBehaviour
    public int times3(Integer n) {
        return n * 3;
    }
}
