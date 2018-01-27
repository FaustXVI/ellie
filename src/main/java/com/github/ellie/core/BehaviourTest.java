package com.github.ellie.core;

public class BehaviourTest {

    public final String name;
    public final Runnable test;

    private BehaviourTest(String name, Runnable test) {
        this.name = name;
        this.test = test;
    }

    static BehaviourTest dynamicTest(String name, Runnable test) {
        return new BehaviourTest(name, test);
    }

}
