package com.github.ellie.core.asserters;

import com.github.ellie.core.Exploration;
import com.github.ellie.core.ExplorationResults;
import com.github.ellie.core.TestResult;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.Exploration.exploration;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryTester implements Tester {

    @Override
    public Stream<Exploration> tests(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        return results.resultByPostConditions()
                      .entrySet()
                      .stream()
                      .map(behaviour -> exploration(
                          behaviour.getKey(),
                          () -> {
                              TestResult testResult = behaviour.getValue();
                              resultConsumer.accept(behaviour.getKey().value, testResult);
                              assertThat(testResult.passingData())
                                  .as("no data validates this behaviour")
                                  .isNotEmpty();
                          }));
    }


}
