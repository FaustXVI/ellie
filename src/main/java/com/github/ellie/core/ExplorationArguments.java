package com.github.ellie.core;

public interface ExplorationArguments {

    Object[] get();

    static ExplorationArguments of(Object... arguments) {
        return () -> arguments;
    }

}
