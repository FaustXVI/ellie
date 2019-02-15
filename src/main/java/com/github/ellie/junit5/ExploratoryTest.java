package com.github.ellie.junit5;

import com.github.ellie.core.explorers.Exploration.ExplorationResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.PASS;

public interface ExploratoryTest {


    public static final BiConsumer<String, ExplorationResult> PRINT_PASSING_CASES = (s, l) -> {
        Stream<String> arguments = l.testResult.argumentsThat(PASS)
                .stream()
                .map(args -> Arrays.stream(args.get())
                        .map(o -> {
                            if (o instanceof String) return "\"" + o + "\"";
                            else return o.toString();
                        })
                        .collect(
                                Collectors.joining(", ", "            Arguments.of(",
                                        ")"))
                );
        String method = arguments
                .collect(Collectors.joining(",\n", "    static Stream<Arguments> " + s + "() {\n"
                        + "        return Stream.of(\n", "\n        );\n    }"));
        System.out.println(method);
    };

    public static final BiConsumer<String, ExplorationResult> PRINT_CORRELATIONS = (s, l) -> {
        l.correlations.forEach(c ->
                System.out.println(c.name + " => " + c.value)
        );
    };

    @TestFactory
    default Stream<? extends DynamicTest> generatedTests() {
        return ExplorerBuilder.generateTestsFor(this)
                .map(t -> DynamicTest.dynamicTest(t.name(), () -> {
                    ExplorationResult result = t.check(m -> {
                        Assertions.assertThat(m.causes)
                                .as(m.message).isEmpty();
                        if(!m.message.isEmpty()){
                            Assertions.fail(m.message);
                        }
                    });
                    testResultConsumer().accept(t.name(), result);
                }));
    }

    default BiConsumer<String, ExplorationResult> testResultConsumer() {
        return PRINT_CORRELATIONS;
    }

}
