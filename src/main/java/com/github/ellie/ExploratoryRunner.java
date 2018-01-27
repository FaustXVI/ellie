package com.github.ellie;

import org.junit.jupiter.api.DynamicTest;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class ExploratoryRunner {


    private final Explorer explorer;
    private BiConsumer<String, Iterable<Object[]>> passingCases;

    private ExploratoryRunner(Explorer explorer, BiConsumer<String, Iterable<Object[]>> passingCases) {
        this.explorer = explorer;
        this.passingCases = passingCases;
    }

    public static Stream<DynamicTest> generateTestsFor(Object testInstance,
                                                       BiConsumer<String, Iterable<Object[]>> passingCases) {
        Explorer explorer = new Explorer(testInstance);
        return new ExploratoryRunner(explorer, passingCases).tests();
    }

    private Stream<DynamicTest> tests() {
        return Stream.concat(testedBehaviours(), Stream.of(unknownBehaviour()));
    }

    private DynamicTest unknownBehaviour() {
        return dynamicTest("Unknown behaviour",
                           assertThat(explorer.dataThatPasses(explorer.unknownBehaviour()))
                               .as("At least one data has unknown behaviour")::isEmpty);
    }

    private Stream<DynamicTest> testedBehaviours() {
        return explorer.behavioursTo(
            behaviour -> dynamicTest(behaviour.toString(),
                                     () -> {
                                         Iterable<Object[]> data = explorer.dataThatPasses(behaviour);
                                         assertThat(data)
                                             .as("no data validates this behaviour")
                                             .isNotEmpty();
                                         passingCases.accept(behaviour.toString(), data);
                                     }));
    }

}
