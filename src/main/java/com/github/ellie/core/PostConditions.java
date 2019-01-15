package com.github.ellie.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class PostConditions {

    public final List<Condition> postConditions;

    public PostConditions(List<Condition> postConditions) {
        this.postConditions = Collections.unmodifiableList(postConditions);
    }

    public PostConditionResults explore(List<ExplorationArguments> data) {
        return new PostConditionResults(data.stream()
                .flatMap(d -> exploreWith(d).stream())
                .collect(toList()));
    }


    private Collection<NamedExecutedCondition> exploreWith(ExplorationArguments arguments) {
        return postConditions.stream()
                .map(e -> new NamedExecutedCondition(e.name(), e.testWith(arguments), arguments))
                .collect(Collectors.toList());
    }
}
