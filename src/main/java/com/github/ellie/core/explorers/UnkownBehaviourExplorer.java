package com.github.ellie.core.explorers;

import com.github.ellie.core.*;
import com.github.ellie.core.Name;

import java.util.Collection;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static com.github.ellie.core.explorers.Exploration.exploration;

public class UnkownBehaviourExplorer implements Explorer {
    private Explorer otherExplorer;

    public UnkownBehaviourExplorer(Explorer otherExplorer) {
        this.otherExplorer = otherExplorer;
    }

    @Override
    public Stream<Exploration> explore(PostConditionResults results, PreConditionResults preConditionResults) {
        return Stream.concat(otherExplorer.explore(results), Stream.of(dataWithUnknownBehaviour(results)));
    }

    private Exploration dataWithUnknownBehaviour(PostConditionResults results) {
        return exploration(new Name("Unknown post-exploration"),
                (errorMessageHandler) -> {
                    TestResult result =
                            results.matchOutputs(b -> b.anyMatch(r -> r == PASS));
                    Collection<ExplorationArguments> dataWithUnknownBehaviour = result.argumentsThat(FAIL);
                    if (!dataWithUnknownBehaviour.isEmpty()) {
                        Exploration.ErrorMessage errorMessage = new Exploration.ErrorMessage("At least one data has unknown post-exploration", dataWithUnknownBehaviour);
                        errorMessageHandler.accept(errorMessage);
                    }
                    Correlations correlations = new Correlations();
                    return new Exploration.ExplorationResult(result, correlations);
                });
    }

}
