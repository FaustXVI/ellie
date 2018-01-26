package com.github.ellie;

import org.junit.jupiter.api.DynamicTest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class ExploratoryRunner {


    public static Stream<DynamicTest> generateTestsFor(Object testInstance) {
        Explorer explorer = new Explorer(testInstance);
        return Stream.concat(testedBehaviours(explorer), unknownBehaviourTest(explorer));
    }

    private static Stream<DynamicTest> unknownBehaviourTest(Explorer explorer) {
        return Stream.of(dynamicTest("Unknown behaviour",
                                     assertThat(explorer.testsThatPasses(explorer.unknownBehaviour()))
                                         .as("At least one data has unknown behaviour")::isEmpty));
    }

    private static Stream<DynamicTest> testedBehaviours(Explorer explorer) {
        return explorer.behavioursTo(
            behaviour -> dynamicTest(behaviour.toString(),
                                     assertThat(explorer.testsThatPasses(behaviour))
                                         .as("no data validates this behaviour")
                                         ::isNotEmpty));
    }

}
