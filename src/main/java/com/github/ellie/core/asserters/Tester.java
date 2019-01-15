package com.github.ellie.core.asserters;

import com.github.ellie.core.PostConditionResults;

import java.util.stream.Stream;

public interface Tester {

    Stream<Exploration> tests(PostConditionResults results);
}
