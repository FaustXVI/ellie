package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.fromPredicate;
import static java.util.stream.Collectors.*;

public interface Explorer {

    Stream<Exploration> explore(PostConditionResults results);

    interface PostConditionResults {
        Map<Name, TestResult> resultByName();

        Map<ExplorationArguments, List<ConditionOutput>> outputByArgument();

        default TestResult matchOutputs(
                Predicate<Stream<ConditionOutput>> postConditionPredicate) {
            Function<List<ConditionOutput>, ConditionOutput> outputFunction
                    = fromPredicate(l -> postConditionPredicate.test(l.stream()));
            Map<ConditionOutput, List<ExplorationArguments>> map = outputByArgument().entrySet().stream()
                    .collect(groupingBy(e -> outputFunction.apply(e.getValue()), mapping(Map.Entry::getKey, toList())));
            return output -> map.getOrDefault(output, Collections.emptyList());
        }
    }
}
