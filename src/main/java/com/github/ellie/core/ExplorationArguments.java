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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArrayExplorationArguments that = (ArrayExplorationArguments) o;
            return Arrays.equals(arguments, that.arguments);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(arguments);
        }
    }
}
