package com.github.ellie;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public interface ExploratoryTest {


    @TestFactory
    default Stream<? extends DynamicNode> generatedTests() {
        return ExploratoryRunner.generateTestsFor(this);
    }

}
