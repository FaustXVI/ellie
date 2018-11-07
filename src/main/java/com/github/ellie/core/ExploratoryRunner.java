package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionTest.postConditionTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryRunner implements Runner {

    private final BiConsumer<String, TestResult> resultConsumer;

    ExploratoryRunner(BiConsumer<String, TestResult> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    @Override
    public Stream<ConditionTest> tests(ExplorationResults results) {
        return results.resultByBehaviour()
                       .entrySet()
                       .stream()
                       .map(behaviour -> postConditionTest(behaviour.getKey(),
                                                           () -> {
                                                               TestResult testResult = behaviour.getValue();
                                                               try {
                                                                   assertThat(testResult.passingData())
                                                                       .as("no data validates this behaviour")
                                                                       .isNotEmpty();
                                                               } finally {
                                                                   resultConsumer.accept(behaviour.getKey(), testResult);
                                                               }
                                                           }));
    }


}
