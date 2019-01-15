package com.github.ellie.junit5;

import com.github.ellie.core.asserters.TestResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ExploratoryTest {


    BiConsumer<String, TestResult> PRINT_PASSING_CASES = (s, l) -> {
        Stream<String> arguments = l.passingData()
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

    @TestFactory
    default Stream<? extends DynamicTest> generatedTests() {
        return RunnerBuilder.generateTestsFor(this)
                .map(t -> DynamicTest.dynamicTest(t.name(), () -> {
                    TestResult result = t.check(m -> Assertions.assertThat(m.causes)
                            .as(m.message).isEmpty());
                    testResultConsumer().accept(t.name(),result);
                }));
    }

    default BiConsumer<String, TestResult> testResultConsumer() {
        return PRINT_PASSING_CASES;
    }

}
