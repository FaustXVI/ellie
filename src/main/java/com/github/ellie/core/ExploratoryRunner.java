package com.github.ellie.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionTest.postConditionTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExploratoryRunner implements Runner {


    private final Explorer explorer;
    private BiConsumer<String, TestResult> resultConsumer;

    ExploratoryRunner(Explorer explorer, BiConsumer<String, TestResult> resultConsumer) {
        this.explorer = explorer;
        this.resultConsumer = resultConsumer;
    }

    @Override
    public Stream<ConditionTest> tests() {
        return explorer.resultByBehaviour()
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
        return explorer.dataThatBehaviours(postConditionPredicate);
    }
}
