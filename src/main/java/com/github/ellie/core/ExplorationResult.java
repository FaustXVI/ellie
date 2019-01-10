package com.github.ellie.core;

import java.util.Optional;

public class ExplorationResult {

    public final Optional<ErrorMessage> error;

    public ExplorationResult(){
        error = Optional.empty();
    }

    public ExplorationResult(ErrorMessage error) {
        this.error = Optional.ofNullable(error);
    }
}
