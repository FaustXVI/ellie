package com.github.ellie.core.asserters;

import com.github.ellie.core.ErrorMessage;
import com.github.ellie.core.Exploration;
import com.github.ellie.core.ExplorationResults;
import com.github.ellie.core.TestResult;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.Exploration.exploration;

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
                            // TODO : move consumer out
                            resultConsumer.accept(behaviour.getKey().value, testResult);
                            if (testResult.passingData().isEmpty()) {
                                return Optional.of(new ErrorMessage("no data validates this behaviour"));
                            } else {
                                return Optional.empty();
                            }
                        }));
    }


}
