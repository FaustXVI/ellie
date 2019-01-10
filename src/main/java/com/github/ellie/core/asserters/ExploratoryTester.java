package com.github.ellie.core.asserters;

import com.github.ellie.core.*;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.github.ellie.core.Exploration.exploration;

public class ExploratoryTester implements Tester {

    @Override
    public Stream<Exploration> tests(PostConditionResults results, BiConsumer<String, TestResult> resultConsumer) {
        return results.resultByPostConditions()
                .entrySet()
                .stream()
                .map(behaviour -> exploration(
                        behaviour.getKey(),
                        (errorHandler) -> {
                            TestResult testResult = behaviour.getValue();
                            // TODO : move consumer out
                            resultConsumer.accept(behaviour.getKey().value, testResult);
                            if (testResult.passingData().isEmpty()) {
                                ErrorMessage errorMessage = new ErrorMessage("no data validates this behaviour");
                                errorHandler.accept(errorMessage);
                            }
                            return testResult;
                        }));
    }


}
