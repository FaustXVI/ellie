package com.github.ellie.core;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionTest.postConditionTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryTester implements Tester {

    @Override
    public Stream<ConditionTest> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return results.resultByBehaviour()
                      .entrySet()
                      .stream()
                      .map(behaviour -> postConditionTest(
                          behaviour.getKey(),
                          () -> {
                              TestResult testResult = behaviour.getValue();
                              resultConsumer.accept(behaviour.getKey(), testResult);
                              assertThat(testResult.passingData())
                                  .as("no data validates this behaviour")
                                  .isNotEmpty();
                          }));
    }


}
