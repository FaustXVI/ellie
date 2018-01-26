package com.github.ellie;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ExploratoryTest {


    @TestFactory
    default Stream<? extends DynamicNode> generatedTests() {
        return ExploratoryRunner.generateTestsFor(this, passingCasesConsumer());
    }

    default Consumer<Object[]> passingCasesConsumer() {
        return o->System.out.println(Arrays.toString(o));
    }

}
