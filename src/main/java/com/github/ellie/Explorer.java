package com.github.ellie;

import org.junit.jupiter.params.provider.Arguments;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.ellie.Behaviour.noneOf;
import static org.assertj.core.api.Assertions.assertThat;

class Explorer {
    private final Object testInstance;
    private final AccessibleMethod testedBehaviour;
    private final List<AccessibleMethod> dataMethods;
    private final List<Behaviour> behaviours;

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

    <T> Stream<T> behavioursTo(Function<Behaviour, T> mapper) {
        return behaviours.stream()
                         .map(mapper);
    }

    Iterable<Object[]> dataThatPasses(Behaviour behaviour) {
        return allData().filter(testBehaviour(behaviour))
                        .map(Arguments::get)
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

    private Predicate<Arguments> testBehaviour(Behaviour behaviour) {
        return (Arguments arguments) -> {
            Object behaviourResult = testedBehaviour.invoke(testInstance, arguments.get());
            Predicate<Object> predicate = behaviour.apply(testInstance, arguments.get());
            return predicate.test(behaviourResult);
        };
    }

    private Stream<Arguments> allData() {
        return dataMethods.stream()
                          .flatMap(this::dataOf)
                          .map(this::toArguments);
    }

    private Stream<?> dataOf(AccessibleMethod f) {
        Object data = f.invoke(testInstance);
        if (data instanceof Stream) return (Stream<?>) data;
        return StreamSupport.stream(((Iterable<?>) data).spliterator(), false);
    }

    private Arguments toArguments(Object o) {
        if (o instanceof Arguments) return ((Arguments) o);
        return Arguments.of(o);
    }

    private class NamedBehaviour implements Behaviour {
        private final AccessibleMethod m;

        NamedBehaviour(AccessibleMethod m) {
            this.m = m;
        }

        @Override
        public Predicate<Object> apply(Object testInstance, Object[] arguments) {
            if (m.returnsAnyOf(Predicate.class)) return (Predicate<Object>) m.invoke(testInstance, arguments);
            return (o) -> {
                try {
                    m.invoke(testInstance, arguments);
                    return true;
                } catch (AssertionError e) {
                    return false;
                }
            };
        }

        @Override
        public String toString() {
            return testedBehaviour.name() + " " + m.name();
        }

    }
}
