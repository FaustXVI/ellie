package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;

public class Exploration {

    public final Name name;
    public final Runnable test;

    private Exploration(Name name, Runnable test) {
        this.name = name;
        this.test = test;
    }

    public static Exploration exploration(Name name, Runnable test) {
        return new Exploration(name, test);
    }

}
