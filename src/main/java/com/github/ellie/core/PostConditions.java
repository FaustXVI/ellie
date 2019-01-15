package com.github.ellie.core;

import com.github.ellie.core.asserters.IPostConditionResults;
import com.github.ellie.core.asserters.TestResult;
import com.github.ellie.core.conditions.ConditionResult;
import com.github.ellie.core.conditions.NamedCondition;
import com.github.ellie.core.conditions.NamedConditionResult;

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

    public IPostConditionResults explore(List<ExplorationArguments> data) {
        return new PostConditionResults(data.stream()
                .flatMap(arguments -> postConditions.stream()
                        .map(e -> e.testWith(arguments)))
                .collect(toList()));
    }


    private static class PostConditionResults implements IPostConditionResults {
       private final Collection<NamedConditionResult> postConditionsResults;

       private PostConditionResults(Collection<NamedConditionResult> postConditionsResults) {
           this.postConditionsResults = postConditionsResults;
       }


       @Override
       public Map<Name, TestResult> resultByPostConditions() {
           return postConditionsResults.stream()
                   .collect(groupingBy(e -> e.name,
                           collectingAndThen(toList(), TestResult::new)));
       }

       @Override
       public TestResult dataThatPostConditions(
               Predicate<Stream<ConditionOutput>> postConditionPredicate) {
           Function<List<ConditionOutput>, ConditionOutput> outputFunction
                   = fromPredicate(l -> postConditionPredicate.test(l.stream()));
           return new TestResult(dataToExecutedConditions(outputFunction));
       }


       private Collection<ConditionResult> dataToExecutedConditions(Function<List<ConditionOutput>, ConditionOutput> outputFunction) {
           return postConditionsResults.stream()
                   .collect(groupingBy(e -> e.arguments,
                           collectingAndThen(mapping(e -> e.output, toList()), outputFunction))
                   ).entrySet().stream()
                   .map(e -> new ConditionResult(e.getValue(), e.getKey()))
                   .collect(toList());
       }

   }
}
