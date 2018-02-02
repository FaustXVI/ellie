package com.github.ellie.core;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.ellie.core.Behaviour.noneOf;

class Explorer {
    private final AccessibleMethod testedBehaviour;
    private final List<NamedBehaviour> behaviours;
    private final List<ExplorationArguments> data;

    Explorer(Object testInstance) {
        MethodFinder methodFinder = new MethodFinder(testInstance);
        this.testedBehaviour = methodFinder.testedBehaviour();
        data = methodFinder.dataProviders()
                           .stream()
                           .flatMap(this::dataOf)
                           .map(this::toArguments)
                           .collect(Collectors.toList());

        this.behaviours = methodFinder.behaviours()
                                      .stream()
                                      .map(NamedBehaviour::new)
                                      .collect(Collectors.toList());
    }

    <T> Stream<T> behavioursTo(Function<NamedBehaviour, T> mapper) {
        return behaviours.stream()
                         .map(mapper);
    }

    Iterable<ExplorationArguments> dataThatPasses(Behaviour behaviour) {
        return allData().filter(testBehaviour(behaviour))
                        .collect(Collectors.toList());
    }

    Behaviour unknownBehaviour() {
        return noneOf(behaviours);
    }

    private Predicate<ExplorationArguments> testBehaviour(Behaviour behaviour) {
        return (ExplorationArguments explorationArguments) -> {
            Object behaviourResult = testedBehaviour.invoke(explorationArguments);
            Predicate<Object> predicate = behaviour.apply(explorationArguments);
            return predicate.test(behaviourResult);
        };
    }

    private Stream<ExplorationArguments> allData() {
        return data.stream();
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
