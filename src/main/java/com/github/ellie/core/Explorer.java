package com.github.ellie.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

class Explorer {
    private final AccessibleMethod testedBehaviour;
    private final List<NamedBehaviour> behaviours;
    private final Map<ExplorationArguments, List<String>> map;

    Explorer(Object testInstance) {
        MethodFinder methodFinder = new MethodFinder(testInstance);
        this.testedBehaviour = methodFinder.testedBehaviour();

        this.behaviours = methodFinder.behaviours()
                                      .stream()
                                      .map(NamedBehaviour::new)
                                      .collect(Collectors.toList());

        List<ExplorationArguments> data = methodFinder.dataProviders()
                                                      .stream()
                                                      .flatMap(this::dataOf)
                                                      .map(this::toArguments)
                                                      .collect(Collectors.toList());


        map = data.stream()
                  .collect(toMap(identity(), this::behavioursFor));
    }

    private List<String> behavioursFor(ExplorationArguments d) {
        return behaviours.stream()
                         .filter(b -> b.apply(d)
                                       .test(testedBehaviour.invoke(d)))
                         .map(NamedBehaviour::name)
                         .collect(Collectors.toList());
    }

    <T> Stream<T> behavioursTo(Function<String, T> mapper) {
        return behaviours.stream()
                         .map(NamedBehaviour::name)
                         .map(mapper);
    }

    Iterable<ExplorationArguments> dataThatPasses(String behaviourName) {
        return dataThatBehaviours(b -> b.contains(behaviourName));
    }

    Iterable<ExplorationArguments> dataThatPassNothing() {
        return dataThatBehaviours(Collection::isEmpty);
    }

    private List<ExplorationArguments> dataThatBehaviours(Predicate<List<String>> behaviourPredicate) {
        return map.entrySet()
                  .stream()
                  .filter(e -> behaviourPredicate.test(e.getValue()))
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
        public Predicate<Object> apply(ExplorationArguments explorationArguments) {
            if (m.returnsAnyOf(Predicate.class))
                return (Predicate<Object>) m.invoke(explorationArguments);
            return (o) -> {
                try {
                    Consumer<Object> consumer = (Consumer<Object>) m.invoke(explorationArguments);
                    consumer.accept(o);
                    return true;
                } catch (AssertionError e) {
                    return false;
                }
            };
        }

        public String name() {
            return testedBehaviour.name() + "_" + m.name();
        }

    }
}
