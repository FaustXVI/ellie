package com.github.ellie.junit5;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.Name;
import com.github.ellie.core.conditions.PostConditions;
import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.conditions.NamedCondition;
import com.github.ellie.core.conditions.NamedConditionResult;
import com.github.ellie.junit5.annotations.DataProvider;
import com.github.ellie.junit5.annotations.TestedBehaviour;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class InstanceParser {
    private Object testInstance;

    InstanceParser(Object testInstance) {
        this.testInstance = testInstance;
    }

    List<ExplorationArguments> data() {
        return dataProviders()
                .stream()
                .flatMap(InstanceParser::dataOf)
                .map(InstanceParser::toArguments)
                .collect(Collectors.toList());
    }

    private static Stream<?> dataOf(AccessibleMethod method) {
        Object data = method.invoke();
        if (data instanceof Stream) return (Stream<?>) data;
        return StreamSupport.stream(((Iterable<?>) data).spliterator(), false);
    }

    private static ExplorationArguments toArguments(Object o) {
        if (o instanceof ExplorationArguments) return ((ExplorationArguments) o);
        return ExplorationArguments.of(o);
    }

    private List<AccessibleMethod> findMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return Arrays.stream(testInstance.getClass()
                .getDeclaredMethods())
                .filter(m -> m.getAnnotation(annotationClass) != null)
                .map(method -> new AccessibleMethod(testInstance, method))
                .collect(Collectors.toList());
    }

    private AccessibleMethod testedBehaviour() {
        List<AccessibleMethod> testedBehaviours = findMethodsAnnotatedWith(TestedBehaviour.class);
        assertThat(testedBehaviours)
                .as("only one method should be annotated with %s", TestedBehaviour.class.getSimpleName())
                .hasSize(1);
        return testedBehaviours.get(0);
    }

    private List<AccessibleMethod> dataProviders() {
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

    private List<AccessibleMethod> postConditions() {
        List<AccessibleMethod> postConditions = findMethodsAnnotatedWith(com.github.ellie.junit5.annotations.PostCondition.class);
        assertThat(postConditions).as(
                com.github.ellie.junit5.annotations.PostCondition.class.getSimpleName() + " methods return type should be predicate or consumer")
                .allMatch(m -> m.returnsAnyOf(Predicate.class, Consumer.class));
        return postConditions;
    }

    PostConditions executablePostConditions() {
        AccessibleMethod exploredCode = testedBehaviour();

        return new PostConditions(postConditions()
                .stream()
                .map(m -> new PostCondition(m,
                        exploredCode))
                .collect(Collectors.toList()));
    }


    private static final class PostCondition implements NamedCondition {
        private final AccessibleMethod postConditionSupplier;
        private final AccessibleMethod behaviourMethod;

        private PostCondition(AccessibleMethod postConditionSupplier, AccessibleMethod behaviourMethod) {
            this.postConditionSupplier = postConditionSupplier;
            this.behaviourMethod = behaviourMethod;
        }

        @Override
        public NamedConditionResult testWith(ExplorationArguments explorationArguments) {
            Predicate<Object> predicate;
            if (postConditionSupplier.returnsAnyOf(Consumer.class)) {
                predicate = asPredicate(postConditionSupplier.invoke(explorationArguments));
            } else {
                predicate = postConditionSupplier.invoke(explorationArguments);
            }
            return new NamedConditionResult(name(), ConditionOutput.fromPredicate(predicate).apply(behaviourMethod.invoke(explorationArguments)), explorationArguments);
        }

        private static Predicate<Object> asPredicate(Consumer<Object> consumer) {
            return (o) -> {
                try {
                    consumer.accept(o);
                    return true;
                } catch (AssertionError e) {
                    return false;
                }
            };
        }

        @Override
        public Name name() {
            return new Name(behaviourMethod.name() + "_" + postConditionSupplier.name());
        }

    }
}
