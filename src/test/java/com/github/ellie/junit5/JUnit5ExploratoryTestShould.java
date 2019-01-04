package com.github.ellie.junit5;

import com.github.ellie.junit5.annotations.DataProvider;
import com.github.ellie.junit5.annotations.PostCondition;
import com.github.ellie.junit5.annotations.TestedBehaviour;
import com.github.ellie.core.Exploration;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.core.TestResult;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.ellie.core.ConditionOutput.PASS;
import static org.assertj.core.api.Assertions.assertThat;

public class JUnit5ExploratoryTestShould {

    private static class PerfectJunit5 implements ExploratoryTest {

        @DataProvider
        public Collection<Integer> numbers() {
            return List.of(2, 4);
        }

        @TestedBehaviour
        public int times2(int n) {
            return n * 2;
        }

        @PostCondition
        public Predicate<Integer> isGreater(int n) {
            return i -> i > n;
        }
    }

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
            assertThat(test.getDisplayName()).isEqualTo(exploration.name);
        }
    }

    @Test
    void prettyPrintsArguments() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        BiConsumer<String, TestResult> printer =
            new PerfectJunit5().passingCasesConsumer();
        printer.accept("test", new TestResult(Map.of(PASS, List.of(ExplorationArguments.of(1, "arg1"), ExplorationArguments.of(2, "arg2")))));
        assertThat(out.toString()).isEqualTo(  "    static Stream<Arguments> test() {\n"
                                             + "        return Stream.of(\n"
                                             + "            Arguments.of(1, \"arg1\"),\n"
                                             + "            Arguments.of(2, \"arg2\")\n"
                                             + "        );\n"
                                             + "    }\n");
    }

    private List<Exploration> explorationOf(PerfectJunit5 junit5Test) {
        return RunnerBuilder.generateTestsFor(junit5Test, (a, b) -> {
        })
                            .collect(Collectors.toList());
    }
}
