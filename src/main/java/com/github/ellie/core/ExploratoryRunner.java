package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.BehaviourTest.dynamicTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryRunner {


    private final Explorer explorer;
    private BiConsumer<String, TestResult> passingCases;

    private ExploratoryRunner(Explorer explorer, BiConsumer<String, TestResult> passingCases) {
        this.explorer = explorer;
        this.passingCases = passingCases;
    }

    public static Stream<BehaviourTest> generateTestsFor(Object testInstance,
                                                         BiConsumer<String, TestResult>
                                                             passingCases) {
        Explorer explorer = new Explorer(testInstance);
        return new ExploratoryRunner(explorer, passingCases).tests();
    }

    private Stream<BehaviourTest> tests() {
        return Stream.concat(Stream.concat(testedBehaviours(), Stream.of(multipleBehaviours())),
                             Stream.of(unknownBehaviour()));
    }

    private BehaviourTest multipleBehaviours() {
        return dynamicTest("Match multiple postConditions", assertThat(explorer.dataThatPassesMultipleBehaviours())
            .as("At least one data has many postConditions")::isEmpty);
    }

    private BehaviourTest unknownBehaviour() {
        return dynamicTest("Unknown behaviour",
                           assertThat(explorer.dataThatPassNothing())
                               .as("At least one data has unknown behaviour")::isEmpty);
    }

    private Stream<BehaviourTest> testedBehaviours() {
        return explorer.resultByBehaviour()
                       .entrySet()
                       .stream()
                       .map(behaviour -> dynamicTest(behaviour.getKey(),
                                                     () -> {
                                                         TestResult testResult = behaviour.getValue();
                                                         assertThat(testResult.passingData())
                                                             .as("no data validates this behaviour")
                                                             .isNotEmpty();
                                                         passingCases.accept(behaviour.getKey(), testResult);
                                                     }));
    }

}
