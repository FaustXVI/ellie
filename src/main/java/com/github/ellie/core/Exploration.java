package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;

public class Exploration {

    public interface Check {
        ExplorationResult check();
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
