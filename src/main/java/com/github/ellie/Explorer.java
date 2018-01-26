package com.github.ellie;

import org.junit.jupiter.params.provider.Arguments;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
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
        this.behaviours = findMethodsAnnotatedWith(PotentialBehaviour.class).stream()
                                                                            .map(NamedBehaviour::new)
                                                                            .collect(Collectors.toList());
    }

    <T> Stream<T> behavioursTo(Function<Behaviour, T> mapper) {
        return behaviours.stream()
                         .map(mapper);
    }

    Stream<Object[]> testsThatPasses(Behaviour behaviour) {
        return allData().filter(testBehaviour(behaviour)::apply);
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

    private Function<Object[], Boolean> testBehaviour(Behaviour behaviour) {
        return (Object[] arguments) -> {
            Object behaviourResult = testedBehaviour.invoke(testInstance, arguments);
            Predicate<Object> predicate = behaviour.apply(testInstance, arguments);
            return predicate.test(behaviourResult);
        };
    }

    private Stream<Object[]> allData() {
        return dataMethods.stream()
                          .flatMap(this::dataOf)
                          .map(this::toArguments);
    }

    private Stream<?> dataOf(AccessibleMethod f) {
        Object data = f.invoke(testInstance);
        if (data instanceof Stream) return (Stream<?>) data;
        return StreamSupport.stream(((Iterable<?>) data).spliterator(), false);
    }

    private Object[] toArguments(Object o) {
        if (o instanceof Arguments) return ((Arguments) o).get();
        return new Object[]{o};
    }

    private class NamedBehaviour implements Behaviour {
        private final AccessibleMethod m;

        public NamedBehaviour(AccessibleMethod m) {
            this.m = m;
        }

        @Override
        public Predicate<Object> apply(Object testInstance, Object[] arguments) {
            return (Predicate<Object>) m.invoke(testInstance, arguments);
        }

        @Override
        public String toString() {
            return testedBehaviour.getName() + " " + m.getName();
        }

    }
}
