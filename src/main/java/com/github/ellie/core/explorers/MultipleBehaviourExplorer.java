package com.github.ellie.core.explorers;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;

import java.util.Collection;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;

public class MultipleBehaviourExplorer implements Explorer {

    private final Explorer explorer;

    public MultipleBehaviourExplorer(Explorer explorer) {
        this.explorer = explorer;
    }

    @Override
    public Stream<Exploration> explore(PostConditionResults results) {
        return Stream.concat(explorer.explore(results),
                dataThatPassesMultiplePostConditions(results));
    }

    private Stream<Exploration> dataThatPassesMultiplePostConditions(PostConditionResults results) {
        return Stream.of(Exploration.exploration(new Name("Match multiple post-conditions"),
                (errorMessageHandler) -> {
                    TestResult testResult = dataThatPassesMaximumOneBehaviour(results);
                    Collection<ExplorationArguments> dataWithMultipleBehaviours = testResult.argumentsThat(FAIL);
                    if (!dataWithMultipleBehaviours.isEmpty()) {
                        Exploration.ErrorMessage errorMessage = new Exploration.ErrorMessage("At least one data has many post-conditions", dataWithMultipleBehaviours);
                        errorMessageHandler.accept(errorMessage);
                    }
                    return testResult;
                }));
    }

    private TestResult dataThatPassesMaximumOneBehaviour(PostConditionResults results) {
        return results.matchOutputs(c -> c.filter(r -> r == PASS)
                .count() <= 1);
    }

}
