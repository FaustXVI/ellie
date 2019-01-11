package com.github.ellie.core;

import com.github.ellie.core.asserters.MultipleBehaviourTester;
import com.github.ellie.core.asserters.Tester;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultipleBehaviourTesterShould {

    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
            NO_MULTIPLE_PASS = Map.of(ExplorationArguments.of(1), Stream.of(PASS),
            ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL),
            ExplorationArguments.of(3), Stream.of(ConditionOutput.IGNORED));
    public static final Map<ExplorationArguments, Stream<ConditionOutput>>
            MULTIPLE_PASS = Map.of(ExplorationArguments.of(2), Stream.of(PASS, PASS));
    private Tester otherTester;
    private MultipleBehaviourTester multipleBehaviourRunner;
    private PostConditionResults results;
    private static final Consumer<ErrorMessage> IGNORE_ERROR_MESSAGE = c -> {
    };


    @BeforeEach
    void createRunner() {
        otherTester = mock(Tester.class);
        results = mock(PostConditionResults.class);
        when(results.dataThatPostConditions(Mockito.any())).thenReturn(new TestResult(Map.of()));
        multipleBehaviourRunner = new MultipleBehaviourTester(otherTester);
    }

    @Test
    void keepsOtherRunnerTests() {
        Exploration test = Exploration.exploration(new Name("test"), (c) -> new TestResult(new ArrayList<>()));
        Mockito.when(otherTester.tests(results))
                .thenReturn(Stream.of(test));

        assertThat(multipleBehaviourRunner.tests(results)).contains(test);
    }

    @Test
    void addsMultipleBehaviourLast() {
        assertThat(multipleBehaviourRunner.tests(results))
                .extracting(Exploration::name)
                .last()
                .isEqualTo(
                        "Match multiple "
                                + "post-conditions");
    }


    @Test
    void callsConsumerWithResults() {
        TestResult testResult = new TestResult(Map.of());
        when(results.dataThatPostConditions(Mockito.any()))
                .thenReturn(testResult);

        multipleBehaviourRunner.tests(results).forEach(ct -> {
            assertThat(ct.name()).isEqualTo("Match multiple post-conditions");
            assertThat(ct.check(IGNORE_ERROR_MESSAGE)).isSameAs(testResult);
        });

    }

    @Test
    void failsIfAtLeastOneDataPassesManyTimes() {
        when(results.dataThatPostConditions(Mockito.any())).then(filterFrom(MULTIPLE_PASS));

        AtomicReference<ErrorMessage> errorMessageAtomicReference = new AtomicReference<>();

        this.multipleBehaviourRunner.tests(results)
                .forEach(t -> t.check(errorMessageAtomicReference::set));

        ErrorMessage errorMessage = errorMessageAtomicReference.get();
        Assertions.assertThat(errorMessage.message)
                .contains("one data has many post-conditions");
        assertThat(errorMessage.causes).containsAll(MULTIPLE_PASS.keySet());

    }

    @Test
    void passesIfNoDataPassesManyTimes() {
        when(results.dataThatPostConditions(Mockito.any()))
                .then(filterFrom(NO_MULTIPLE_PASS));

        try {
            multipleBehaviourRunner.tests(results)
                    .forEach(t -> t.check(IGNORE_ERROR_MESSAGE));
        } catch (Exception e) {
            fail("No data passes many post conditions");
        }
    }

    public static Answer<TestResult> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return invocationOnMock -> {
            Predicate<Stream<ConditionOutput>> predicate = invocationOnMock.getArgument(0);
            Map<ConditionOutput, List<ExplorationArguments>> results =
                    data.entrySet()
                            .stream()
                            .collect(groupingBy(
                                    e -> predicate.test(e.getValue()) ? PASS
                                            : FAIL,
                                    mapping(Map.Entry::getKey,
                                            toList())
                            ));
            return new TestResult(results);
        };
    }


}
