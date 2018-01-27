package com.github.ellie.core;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.ellie.core.Behaviour.noneOf;
import static org.assertj.core.api.Assertions.assertThat;

class Explorer {
    private final Object testInstance;
    private final AccessibleMethod testedBehaviour;
    private final List<AccessibleMethod> dataMethods;
    private final List<NamedBehaviour> behaviours;

    Explorer(Object testInstance) {
        this.testInstance = testInstance;
        List<AccessibleMethod> testedBehaviours = findMethodsAnnotatedWith(TestedBehaviour.class);
        assertThat(testedBehaviours)
            .as("only one method should be annotated with %s", TestedBehaviour.class.getSimpleName())
            .hasSize(1);
        this.testedBehaviour = testedBehaviours.get(0);
        this.dataMethods = findMethodsAnnotatedWith(DataProvider.class);
        assertThat(dataMethods.size())
            .as("no data found : at least one method should be annotated with %s",
                DataProvider.class.getSimpleName())
            .isGreaterThanOrEqualTo(1);
        assertThat(dataMethods).as(
            DataProvider.class.getSimpleName() + " methods return type should be iterable or stream")
                               .allMatch((m) -> m.returnsAnyOf(Stream.class, Iterable.class));
        List<AccessibleMethod> potentialBehaviours = findMethodsAnnotatedWith(PotentialBehaviour.class);
        assertThat(potentialBehaviours).as(
            PotentialBehaviour.class.getSimpleName() + " methods return type should be predicate or consumer")
                                       .allMatch(m -> m.returnsAnyOf(Predicate.class, Consumer.class));
        this.behaviours = potentialBehaviours.stream()
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

    private List<AccessibleMethod> findMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return Arrays.stream(testInstance.getClass()
                                         .getDeclaredMethods())
                     .filter(m -> m.getAnnotation(annotationClass) != null)
                     .map(AccessibleMethod::new)
                     .collect(Collectors.toList());
    }

    private Predicate<ExplorationArguments> testBehaviour(Behaviour behaviour) {
        return (ExplorationArguments explorationArguments) -> {
            Object behaviourResult = testedBehaviour.invoke(testInstance, explorationArguments);
            Predicate<Object> predicate = behaviour.apply(testInstance, explorationArguments);
            return predicate.test(behaviourResult);
        };
    }

    private Stream<ExplorationArguments> allData() {
        return dataMethods.stream()
                          .flatMap(this::dataOf)
                          .map(this::toArguments);
    }

    private Stream<?> dataOf(AccessibleMethod method) {
        Object data = method.invoke(testInstance);
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
        public Predicate<Object> apply(Object testInstance, ExplorationArguments explorationArguments) {
            if (m.returnsAnyOf(Predicate.class)) return (Predicate<Object>) m.invoke(testInstance, explorationArguments);
            return (o) -> {
                try {
                    Consumer<Object> consumer = (Consumer<Object>) m.invoke(testInstance, explorationArguments);
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
