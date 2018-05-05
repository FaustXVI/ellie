package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.PostConditionTest.postConditionTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryRunner {


    private final Explorer explorer;
    private BiConsumer<String, TestResult> passingCases;

    private ExploratoryRunner(Explorer explorer, BiConsumer<String, TestResult> passingCases) {
        this.explorer = explorer;
        this.passingCases = passingCases;
    }

    public static Stream<PostConditionTest> generateTestsFor(Object testInstance,
                                                             BiConsumer<String, TestResult>
                                                                 passingCases) {
        return exploratoryRunnerFor(testInstance, passingCases).tests();
    }

    static ExploratoryRunner exploratoryRunnerFor(Object testInstance,
                                                  BiConsumer<String, TestResult> passingCases) {
        Explorer explorer = new Explorer(testInstance);
        return new ExploratoryRunner(explorer, passingCases);
    }

    Stream<PostConditionTest> tests() {
        return Stream.concat(testedBehaviours(),
                             Stream.of(multipleBehaviours(), unknownBehaviour(), perfectDefinition()));
    }

    PostConditionTest multipleBehaviours() {
        return postConditionTest("Match multiple post-conditions",
                                 assertThat(explorer.dataThatPassesMultipleBehaviours())
                                     .as("At least one data has many post-conditions")::isEmpty);
    }

    PostConditionTest unknownBehaviour() {
        return postConditionTest("Unknown post-condition",
                                 assertThat(explorer.dataThatPassNothing())
                                     .as("At least one data has unknown post-condition")::isEmpty);
    }

    Stream<PostConditionTest> testedBehaviours() {
        return explorer.resultByBehaviour()
                       .entrySet()
                       .stream()
                       .map(behaviour -> postConditionTest(behaviour.getKey(),
                                                           () -> {
                                                               TestResult testResult = behaviour.getValue();
                                                               assertThat(testResult.passingData())
                                                                   .as("no data validates this behaviour")
                                                                   .isNotEmpty();
                                                               passingCases.accept(behaviour.getKey(), testResult);
                                                           }));
    }

    public PostConditionTest perfectDefinition() {
        return postConditionTest("Perfect definition", assertThat(explorer.dataThatFailedOnce())
            .as("one data has failed at least one behaviour")::isEmpty);
    }
}
