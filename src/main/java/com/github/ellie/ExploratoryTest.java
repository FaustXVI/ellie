package com.github.ellie;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface ExploratoryTest {


    @TestFactory
    default Stream<? extends DynamicNode> generatedTests() {
        return ExploratoryRunner.generateTestsFor(this, passingCasesConsumer());
    }

    default BiConsumer<String,Iterable<Object[]>> passingCasesConsumer() {
        return (s,l) -> l.forEach(o->System.out.println(Arrays.toString(o)));
    }

}
