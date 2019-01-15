package com.github.ellie.core;

import java.util.Arrays;

public interface ExplorationArguments {

    Object[] get();

    static ExplorationArguments of(Object... arguments) {
        return new ArrayExplorationArguments(arguments);
    }

    class ArrayExplorationArguments implements ExplorationArguments {
        private final Object[] arguments;

        private ArrayExplorationArguments(Object... arguments) {
            this.arguments = arguments;
        }

        @Override
        public Object[] get() {
            return arguments;
        }

        @Override
        public String toString() {
            return Arrays.toString(arguments);
        }

    }
}
