package com.github.ellie.core.asserters;

import java.util.stream.Stream;

import static com.github.ellie.core.asserters.Exploration.exploration;

public class ExploratoryTester implements Tester {

    @Override
    public Stream<Exploration> tests(IPostConditionResults results) {
        return results.resultByPostConditions()
                .entrySet()
                .stream()
                .map(behaviour -> exploration(
                        behaviour.getKey(),
                        (errorHandler) -> {
                            TestResult testResult = behaviour.getValue();
                            if (testResult.passingData().isEmpty()) {
                                ErrorMessage errorMessage = new ErrorMessage("no data validates this behaviour");
                                errorHandler.accept(errorMessage);
                            }
                            return testResult;
                        }));
    }


}
