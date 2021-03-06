package com.github.ellie.junit5;

import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.MapTestResult;
import com.github.ellie.core.explorers.Correlation;
import com.github.ellie.core.explorers.Correlations;
import com.github.ellie.core.explorers.Exploration;
import com.github.ellie.junit5.examples.PerfectJunit5;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;

public class JUnit5ExploratoryTestShould {

    @Test
    void createDynamicTests() {
        PerfectJunit5 junit5Test = new PerfectJunit5();
        List<? extends DynamicTest> tests = junit5Test.generatedTests()
                .collect(Collectors.toList());
        List<Exploration> explorations = explorationOf(junit5Test);
        assertThat(tests).hasSameSizeAs(explorations);
        for (int i = 0; i < tests.size(); i++) {
            DynamicTest test = tests.get(i);
            Exploration exploration = explorations.get(i);
            assertThat(test.getDisplayName()).isEqualTo(exploration.name());
        }
    }

    @Test
    void prettyPrintsArguments() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        BiConsumer<String, Exploration.ExplorationResult> printer = ExploratoryTest.PRINT_PASSING_CASES;
        printer.accept("test", new Exploration.ExplorationResult(new MapTestResult(Map.of(
                PASS,
                List.of(ExplorationArguments.of(1, "arg1"),
                        ExplorationArguments.of(2, "arg2")))), new Correlations())
        );
        assertThat(out.toString()).isEqualTo("    static Stream<Arguments> test() {\n"
                + "        return Stream.of(\n"
                + "            Arguments.of(1, \"arg1\"),\n"
                + "            Arguments.of(2, \"arg2\")\n"
                + "        );\n"
                + "    }\n");
    }

    @Test
    void printsCorrelations() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        BiConsumer<String, Exploration.ExplorationResult> printer = ExploratoryTest.PRINT_CORRELATIONS;
        printer.accept("test", new Exploration.ExplorationResult(new MapTestResult(Map.of(
                PASS,
                List.of(ExplorationArguments.of(1, "arg1"),
                        ExplorationArguments.of(2, "arg2")))),
                new Correlations(List.of(new Correlation("precondition", 0.5),
                        new Correlation("precondition2", 0.75))))
        );
        assertThat(out.toString()).isEqualTo("precondition => 0.5\n" +
                "precondition2 => 0.75\n");
    }

    @Test
    void callsOverridableConsumer() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        BiConsumer<String, Exploration.ExplorationResult> printer = new PerfectJunit5() {
            @Override
            public BiConsumer<String, Exploration.ExplorationResult> testResultConsumer() {
                return (s, t) -> System.out.print(s);
            }
        }.testResultConsumer();
        printer.accept("test", new Exploration.ExplorationResult(new MapTestResult(Map.of(
                PASS,
                List.of(ExplorationArguments.of(1, "arg1"),
                        ExplorationArguments.of(2, "arg2")))), new Correlations())
        );
        assertThat(out.toString()).isEqualTo("test");
    }

    private List<Exploration> explorationOf(PerfectJunit5 junit5Test) {
        return ExplorerBuilder.generateTestsFor(junit5Test)
                .collect(Collectors.toList());
    }
}
