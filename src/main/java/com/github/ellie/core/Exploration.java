package com.github.ellie.core;

public class Exploration {

    public final String name;
    public final Runnable test;

    private Exploration(String name, Runnable test) {
        this.name = name;
        this.test = test;
    }

    public static Exploration exploration(String name, Runnable test) {
        return new Exploration(name, test);
    }

}
