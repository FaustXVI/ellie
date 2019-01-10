package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;

import java.util.Optional;

public class Exploration {

    public interface Check {
        // TODO create Object in order to add the test results
        Optional<ErrorMessage> check();
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
