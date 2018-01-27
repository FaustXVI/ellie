package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.BehaviourTest.dynamicTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryRunner {


    private final Explorer explorer;
    private BiConsumer<String, Iterable<Object[]>> passingCases;

    private ExploratoryRunner(Explorer explorer, BiConsumer<String, Iterable<Object[]>> passingCases) {
        this.explorer = explorer;
        this.passingCases = passingCases;
    }

    public static Stream<BehaviourTest> generateTestsFor(Object testInstance,
                                                         BiConsumer<String, Iterable<Object[]>> passingCases) {
        Explorer explorer = new Explorer(testInstance);
        return new ExploratoryRunner(explorer, passingCases).tests();
    }

    private Stream<BehaviourTest> tests() {
        return Stream.concat(testedBehaviours(), Stream.of(unknownBehaviour()));
    }

    private BehaviourTest unknownBehaviour() {
        return dynamicTest("Unknown behaviour",
                           assertThat(explorer.dataThatPasses(explorer.unknownBehaviour()))
                               .as("At least one data has unknown behaviour")::isEmpty);
    }

    private Stream<BehaviourTest> testedBehaviours() {
        return explorer.behavioursTo(
            behaviour -> dynamicTest(behaviour.name(),
                                     () -> {
                                         Iterable<Object[]> data = explorer.dataThatPasses(behaviour);
                                         assertThat(data)
                                             .as("no data validates this behaviour")
                                             .isNotEmpty();
                                         passingCases.accept(behaviour.name(), data);
                                     }));
    }

}
