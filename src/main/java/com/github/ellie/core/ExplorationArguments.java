package com.github.ellie.core;

import java.util.Arrays;

public interface ExplorationArguments {

    Object[] get();

    static ExplorationArguments of(Object... arguments) {
        return new ExplorationArguments() {
            @Override
            public Object[] get() {
                return arguments;
            }

            @Override
            public String toString() {
                return Arrays.toString(arguments);
            }
        };
    }

}
