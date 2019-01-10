package com.github.ellie.core;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Explorer {

    public static PostConditionResults explore(List<ExplorationArguments> data, PostConditions postConditions) {
        return new PostConditionResults(data.stream()
                .flatMap(d -> postConditions.exploreWith(d).stream())
                .collect(toList()));
    }

}
