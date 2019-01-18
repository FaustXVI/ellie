package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.conditions.ConditionResult;
import com.github.ellie.core.explorers.Exploration.ExplorationResult;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static java.util.stream.Collectors.toList;

public class ExplorerFixtures {
    public static final TestResult EMPTY_TEST_RESULT = o -> Collections.emptyList();
    public static final ExplorationResult EMPTY_EXPLORATION_RESULT = new ExplorationResult(o -> Collections.emptyList(), new Correlations());
    static final Consumer<Exploration.ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };

    public static Answer<TestResult> filterFrom(Map<ExplorationArguments, Collection<ConditionOutput>> data) {
        return invocationOnMock -> {
            Predicate<Stream<ConditionOutput>> predicate = invocationOnMock.getArgument(0);
            List<ConditionResult> results =
                    data.entrySet()
                            .stream()
                            .map(e -> new ConditionResult(predicate.test(e.getValue().stream()) ? PASS
                                    : FAIL, e.getKey()))
                            .collect(toList());
            return o -> results.stream().filter(r -> o == r.output).map(r -> r.arguments).collect(toList());
        };
    }
}
