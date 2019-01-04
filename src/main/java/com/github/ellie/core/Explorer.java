package com.github.ellie.core;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Explorer {

    public static ExplorationResults explore(List<ExplorationArguments> data, PostConditions postConditions) {
        return new ExplorationResults(data.stream()
                .flatMap(d -> postConditions.exploreWith(d).stream())
                .collect(toList()));
    }

}
