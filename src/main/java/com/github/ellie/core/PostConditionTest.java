package com.github.ellie.core;

public class PostConditionTest {

    public final String name;
    public final Runnable test;

    private PostConditionTest(String name, Runnable test) {
        this.name = name;
        this.test = test;
    }

    static PostConditionTest postConditionTest(String name, Runnable test) {
        return new PostConditionTest(name, test);
    }

}
