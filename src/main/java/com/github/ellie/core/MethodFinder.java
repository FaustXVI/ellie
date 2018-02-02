package com.github.ellie.core;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MethodFinder {
    private Object testInstance;

    MethodFinder(Object testInstance) {
        this.testInstance = testInstance;
    }

    List<AccessibleMethod> findMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return Arrays.stream(testInstance.getClass()
                                         .getDeclaredMethods())
                     .filter(m -> m.getAnnotation(annotationClass) != null)
                     .map(method -> new AccessibleMethod(testInstance, method))
                     .collect(Collectors.toList());
    }

    AccessibleMethod testedBehaviour() {
        List<AccessibleMethod> testedBehaviours = findMethodsAnnotatedWith(TestedBehaviour.class);
        assertThat(testedBehaviours)
            .as("only one method should be annotated with %s", TestedBehaviour.class.getSimpleName())
            .hasSize(1);
        return testedBehaviours.get(0);
    }

    List<AccessibleMethod> dataProviders() {
        List<AccessibleMethod> dataMethods = findMethodsAnnotatedWith(DataProvider.class);
        assertThat(dataMethods.size())
            .as("no data found : at least one method should be annotated with %s",
                DataProvider.class.getSimpleName())
            .isGreaterThanOrEqualTo(1);
        assertThat(dataMethods).as(
            DataProvider.class.getSimpleName() + " methods return type should be iterable or stream")
                               .allMatch((m) -> m.returnsAnyOf(Stream.class, Iterable.class));
        return dataMethods;
    }

    List<AccessibleMethod> behaviours() {
        List<AccessibleMethod> potentialBehaviours = findMethodsAnnotatedWith(PotentialBehaviour.class);
        assertThat(potentialBehaviours).as(
            PotentialBehaviour.class.getSimpleName() + " methods return type should be predicate or consumer")
                                       .allMatch(m -> m.returnsAnyOf(Predicate.class, Consumer.class));
        return potentialBehaviours;
    }
}
