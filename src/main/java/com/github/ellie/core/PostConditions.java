package com.github.ellie.core;

import com.github.ellie.core.ExplorableCondition.Name;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class PostConditions {

    public final List<ExplorableCondition> postConditions;

    public PostConditions(List<ExplorableCondition> postConditions) {
        this.postConditions = Collections.unmodifiableList(postConditions);
    }


    public Map<Name, ConditionOutput> exploreWith(ExplorationArguments d) {
        return postConditions.stream()
                .collect(toMap(ExplorableCondition::name, b -> b.testWith(d)));
    }
}
