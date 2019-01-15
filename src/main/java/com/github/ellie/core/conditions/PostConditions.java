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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.fromPredicate;
import static java.util.stream.Collectors.*;

public class PostConditions {

    public final List<NamedCondition> postConditions;

    public PostConditions(List<NamedCondition> postConditions) {
        this.postConditions = Collections.unmodifiableList(postConditions);
    }

    public Explorer.PostConditionResults explore(List<ExplorationArguments> data) {
        return new PostConditionResults(data.stream()
                .flatMap(arguments -> postConditions.stream()
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
                           collectingAndThen(toList(), TestResult::new)));
       }

       @Override
       public TestResult matchOutputs(
               Predicate<Stream<ConditionOutput>> postConditionPredicate) {
           Function<List<ConditionOutput>, ConditionOutput> outputFunction
                   = fromPredicate(l -> postConditionPredicate.test(l.stream()));
           return new TestResult(conditionResultsFor(outputFunction));
       }


       private Collection<ConditionResult> conditionResultsFor(Function<List<ConditionOutput>, ConditionOutput> outputFunction) {
           return postConditionsResults.stream()
                   .collect(groupingBy(e -> e.arguments,
                           collectingAndThen(mapping(e -> e.output, toList()), outputFunction))
                   ).entrySet().stream()
                   .map(e -> new ConditionResult(e.getValue(), e.getKey()))
                   .collect(toList());
       }

   }
}
