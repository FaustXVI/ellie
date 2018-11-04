package com.github.ellie.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionTest.postConditionTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryRunner implements Runner {

    private final ExplorationResults results;
    private BiConsumer<String, TestResult> resultConsumer;

    ExploratoryRunner(ExplorationResults results, BiConsumer<String, TestResult> resultConsumer) {
        this.results = results;
        this.resultConsumer = resultConsumer;
    }

    // TODO transform into a pure function from results to test
    @Override
    public Stream<ConditionTest> tests() {
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


    @Override
    public List<ExplorationArguments> dataThatBehaviours(
        Predicate<Stream<ConditionOutput>> postConditionPredicate) {
        return results.dataThatBehaviours(postConditionPredicate);
    }
}
