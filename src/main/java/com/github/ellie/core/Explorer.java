package com.github.ellie.core;

import org.opentest4j.TestAbortedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.ellie.core.PostConditionOutput.FAIL;
import static com.github.ellie.core.PostConditionOutput.IGNORED;
import static com.github.ellie.core.PostConditionOutput.PASS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;

class Explorer {
    private final AccessibleMethod exploredCode;
    private final List<NamedBehaviour> postConditions;
    private final Map<ExplorationArguments, Map<String, PostConditionOutput>> dataToPostConditionsResults;

    Explorer(Object testInstance) {
        MethodFinder methodFinder = new MethodFinder(testInstance);
        this.exploredCode = methodFinder.testedBehaviour();

        this.postConditions = methodFinder.postConditions()
                                          .stream()
                                          .map(NamedBehaviour::new)
                                          .collect(Collectors.toList());

        List<ExplorationArguments> data = methodFinder.dataProviders()
                                                      .stream()
                                                      .flatMap(this::dataOf)
                                                      .map(this::toArguments)
                                                      .collect(Collectors.toList());


        dataToPostConditionsResults = data.stream()
                                          .collect(toMap(identity(), this::testPostConditions));
    }

    private Map<String, PostConditionOutput> testPostConditions(ExplorationArguments d) {
        return postConditions.stream()
                             .collect(toMap(NamedBehaviour::name, b -> b.apply(d)
                                                                        .test(exploredCode.invoke(d))));
    }

    Map<String, TestResult> resultByBehaviour() {
        return postConditions.stream()
                             .map(NamedBehaviour::name)
                             .collect(toMap(identity(), this::resultsFor));
    }

    Collection<ExplorationArguments> dataThatPassNothing() {
        return dataThatBehaviours(b -> b.values()
                                        .stream()
                                        .noneMatch(r -> r == PASS));
    }

    Collection<ExplorationArguments> dataThatPassesMultipleBehaviours() {
        return dataThatBehaviours(c -> c.values()
                                        .stream()
                                        .filter(r -> r == PASS)
                                        .count() > 1);
    }


    Collection<ExplorationArguments> dataThatFailedOnce() {
        return dataThatBehaviours(c -> c.values()
                                        .stream()
                                        .filter(r -> r == FAIL)
                                        .count() >= 1);
    }

    private TestResult resultsFor(String behaviourName) {
        return new TestResult(
            dataToPostConditionsResults.entrySet()
                                       .stream()
                                       .collect(groupingBy(e -> e.getValue()
                                                                 .get(behaviourName),
                                                           mapping(Map.Entry::getKey, Collectors.toList()))));
    }

    private List<ExplorationArguments> dataThatBehaviours(
        Predicate<Map<String, PostConditionOutput>> postConditionPredicate) {
        return dataToPostConditionsResults.entrySet()
                                          .stream()
                                          .filter(e -> postConditionPredicate.test(e.getValue()))
                                          .map(Map.Entry::getKey)
                                          .collect(Collectors.toList());
    }

    private Stream<?> dataOf(AccessibleMethod method) {
        Object data = method.invoke();
        if (data instanceof Stream) return (Stream<?>) data;
        return StreamSupport.stream(((Iterable<?>) data).spliterator(), false);
    }

    private ExplorationArguments toArguments(Object o) {
        if (o instanceof ExplorationArguments) return ((ExplorationArguments) o);
        return ExplorationArguments.of(o);
    }

    public final class NamedBehaviour implements Behaviour {
        private final AccessibleMethod m;

        NamedBehaviour(AccessibleMethod m) {
            this.m = m;
        }

        @Override
        public BehaviourExecutable apply(ExplorationArguments explorationArguments) {
            if (m.returnsAnyOf(Predicate.class))
                return (o) -> {
                    try {
                        return ((Predicate<Object>) m.invoke(explorationArguments)).test(o) ? PASS : FAIL;
                    } catch (TestAbortedException e) {
                        return IGNORED;
                    }
                };
            return (o) -> {
                try {
                    Consumer<Object> consumer = (Consumer<Object>) m.invoke(explorationArguments);
                    consumer.accept(o);
                    return PASS;
                } catch (AssertionError e) {
                    return FAIL;
                } catch (TestAbortedException e) {
                    return IGNORED;
                }
            };
        }

        public String name() {
            return exploredCode.name() + "_" + m.name();
        }

    }

}
