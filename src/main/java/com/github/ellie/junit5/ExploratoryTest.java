package com.github.ellie.junit5;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.ExploratoryRunner;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public interface ExploratoryTest {


    @TestFactory
    default Stream<? extends DynamicNode> generatedTests() {
        return ExploratoryRunner.generateTestsFor(this, passingCasesConsumer())
            .map(t-> DynamicTest.dynamicTest(t.name, t.test::run));
    }

    default BiConsumer<String,Iterable<ExplorationArguments>> passingCasesConsumer() {
        return (s,l) -> l.forEach(o->System.out.println(Arrays.toString(o.get())));
    }

}
