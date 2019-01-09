package com.github.ellie.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class ErrorMessage {

    public final String message;
    public final Collection<ExplorationArguments> causes;

    public ErrorMessage(String message) {
        this(message, Collections.emptyList());
    }

    public ErrorMessage(String message, Collection<ExplorationArguments> causes) {
        this.message = message;
        this.causes = causes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorMessage that = (ErrorMessage) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }
}
