package com.github.ellie.core;

import com.github.ellie.core.conditions.NamedCondition;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PostConditions {

    public final List<NamedCondition> postConditions;

    public PostConditions(List<NamedCondition> postConditions) {
        this.postConditions = Collections.unmodifiableList(postConditions);
    }

    public PostConditionResults explore(List<ExplorationArguments> data) {
        return new PostConditionResults(data.stream()
                .flatMap(arguments -> postConditions.stream()
                        .map(e -> e.testWith(arguments)))
                .collect(toList()));
    }


}
