package com.github.ellie.junit5;

import com.github.ellie.core.ExploratoryRunner;
import com.github.ellie.core.TestResult;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ExploratoryTest {


    @TestFactory
    default Stream<? extends DynamicTest> generatedTests() {
        return ExploratoryRunner.generateTestsFor(this, passingCasesConsumer())
                                .map(t -> DynamicTest.dynamicTest(t.name, t.test::run));
    }

    default BiConsumer<String, TestResult> passingCasesConsumer() {
        return (s, l) -> {
            Stream<String> arguments = l.passingData().stream()
                                           .map(args -> Arrays.stream(args.get())
                                                              .map(o->{
if(o instanceof String) return "\""+o+"\"";
else return o.toString();
                                                              })
                                                              .collect(
                                                                  Collectors.joining(", ", "            Arguments.of(", ")"))
                                           );
            String method = arguments
                .collect(Collectors.joining(",\n", "    static Stream<Arguments> " + s + "() {\n"
                                                 + "        return Stream.of(\n", "\n        );\n    }"));
            System.out.println(method);
        };
    }

}
