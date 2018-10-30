package com.github.ellie.core;

public class ConditionTest {

    public final String name;
    public final Runnable test;

    private ConditionTest(String name, Runnable test) {
        this.name = name;
        this.test = test;
    }

    static ConditionTest postConditionTest(String name, Runnable test) {
        return new ConditionTest(name, test);
    }

}
