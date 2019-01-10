package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;

import java.util.function.Consumer;

public class Exploration {

    public interface Check {
        TestResult check(Consumer<ErrorMessage> errorHandler);
    }

    public final Name name;
    public final Check test;

    private Exploration(Name name, Check test) {
        this.name = name;
        this.test = test;
    }

    public static Exploration exploration(Name name, Check test) {
        return new Exploration(name, test);
    }

}
