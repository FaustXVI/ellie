package com.github.ellie.core.explorers;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

public class Exploration {

    public interface Check {
        TestResult check(Consumer<ErrorMessage> errorHandler);
    }

    private final Name name;
    private final Check test;

    private Exploration(Name name, Check test) {
        this.name = name;
        this.test = test;
    }

    public TestResult check(Consumer<ErrorMessage> errorHandler){
        return test.check(errorHandler);
    }

    public String name(){
        return name.value;
    }

    public static Exploration exploration(Name name, Check test) {
        return new Exploration(name, test);
    }

    public static class ErrorMessage {

        public final String message;
        public final Collection<ExplorationArguments> causes;

        ErrorMessage(String message) {
            this(message, Collections.emptyList());
        }

        ErrorMessage(String message, Collection<ExplorationArguments> causes) {
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
}
