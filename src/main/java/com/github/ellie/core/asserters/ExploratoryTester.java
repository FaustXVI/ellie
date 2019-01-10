package com.github.ellie.core.asserters;

import com.github.ellie.core.ErrorMessage;
import com.github.ellie.core.Exploration;
import com.github.ellie.core.PostConditionResults;
import com.github.ellie.core.TestResult;

import java.util.stream.Stream;

import static com.github.ellie.core.Exploration.exploration;

public class ExploratoryTester implements Tester {

    @Override
    public Stream<Exploration> tests(PostConditionResults results) {
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
