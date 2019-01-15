package com.github.ellie.core.asserters;

import java.util.stream.Stream;

public interface Tester {

    Stream<Exploration> tests(IPostConditionResults results);
}
