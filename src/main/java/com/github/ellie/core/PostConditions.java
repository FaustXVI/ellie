package com.github.ellie.core;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PostConditions {

    public final List<Condition> postConditions;

    public PostConditions(List<Condition> postConditions) {
        this.postConditions = Collections.unmodifiableList(postConditions);
    }

    public PostConditionResults explore(List<ExplorationArguments> data) {
        return new PostConditionResults(data.stream()
                .flatMap(arguments -> postConditions.stream()
                        .map(e -> new NamedExecutedCondition(e.name(), e.testWith(arguments), arguments)))
                .collect(toList()));
    }


}
