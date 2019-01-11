package com.github.ellie.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PostConditions {

    public final List<ExplorableCondition> postConditions;

    public PostConditions(List<ExplorableCondition> postConditions) {
        this.postConditions = Collections.unmodifiableList(postConditions);
    }


    public Collection<ExecutedCondition> exploreWith(ExplorationArguments d) {
        return postConditions.stream()
                .map(e -> new ExecutedCondition(e.name(), e.testWith(d), d))
                .collect(Collectors.toList());
    }
}
