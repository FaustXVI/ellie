package com.github.ellie.core.conditions;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import com.github.ellie.core.explorers.Explorer;
import com.github.ellie.core.explorers.TestResult;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public class PostConditions {

    private final List<NamedCondition> conditions;

    public PostConditions(List<NamedCondition> conditions) {
        this.conditions = Collections.unmodifiableList(conditions);
    }

    public Explorer.PostConditionResults explore(List<ExplorationArguments> data) {
        return new PostConditionResults(data.stream()
                .flatMap(arguments -> conditions.stream()
                        .map(e -> e.testWith(arguments)))
                .collect(toList()));
    }


    private static class PostConditionResults implements Explorer.PostConditionResults {
        private final Collection<NamedConditionResult> postConditionsResults;

        private PostConditionResults(Collection<NamedConditionResult> postConditionsResults) {
            this.postConditionsResults = postConditionsResults;
        }


        @Override
        public Map<Name, TestResult> resultByName() {
            return postConditionsResults.stream()
                    .collect(groupingBy(e -> e.name,
                            collectingAndThen(toList(), UnmodifiableTestResult::new)));
        }

        @Override
        public Map<ExplorationArguments, List<ConditionOutput>> outputByArgument() {
            return postConditionsResults.stream()
                    .collect(groupingBy(e -> e.arguments,
                            mapping(e -> e.output, toList())));
        }

    }
}
