package com.github.ellie.core.explorers;

import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.explorers.Exploration.exploration;

public class AtLeastOneMatchExplorer implements Explorer {

    @Override
    public Stream<Exploration> explore(PostConditionResults results, PreConditionResults preConditionResults) {
        return results.resultByName()
                .entrySet()
                .stream()
                .map(behaviour -> exploration(
                        behaviour.getKey(),
                        (errorHandler) -> {
                            TestResult testResult = behaviour.getValue();
                            if (testResult.argumentsThat(PASS).isEmpty()) {
                                Exploration.ErrorMessage errorMessage = new Exploration.ErrorMessage("no data validates this behaviour");
                                errorHandler.accept(errorMessage);
                            }
                            Correlations correlations = preConditionResults.correlationsWith(testResult);
                            return new Exploration.ExplorationResult(testResult, correlations);
                        }));
    }


}
