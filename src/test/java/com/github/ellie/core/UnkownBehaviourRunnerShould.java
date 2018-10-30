package com.github.ellie.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnkownBehaviourRunnerShould {

    private Runner otherRunner;
    private Runner exploratoryRunner;

    @BeforeEach
    void createRunner() {
        otherRunner = mock(Runner.class);
        exploratoryRunner = new UnkownBehaviourRunner(otherRunner);
    }

    @Test
    void keepsOtherRunnerTests() {
        ConditionTest test = ConditionTest.postConditionTest("test", () -> {
        });
        Mockito.when(otherRunner.tests())
               .thenReturn(Stream.of(test));

        assertThat(exploratoryRunner.tests()).contains(test);
    }

    @Test
    void addsUnknownBehaviourLast() {
        assertThat(exploratoryRunner.tests()).extracting(b -> b.name)
                                             .last()
                                             .isEqualTo("Unknown post-condition");
    }


    @Test
    void failsIfAtLeastOneDataPassesNothing() {
        when(otherRunner.dataThatBehaviours(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(2), Stream.of(ConditionOutput.FAIL))));

        Assertions.assertThatThrownBy(() -> this.exploratoryRunner.tests()
                                                                  .forEach(t -> t.test.run()))
                  .isInstanceOf(AssertionError.class)
                  .hasMessageContaining("At least one data has unknown post-condition")
                  .hasMessageContaining("2");
    }

    @Test
    void passesIfAllDataPassesSomeThing() {
        when(otherRunner.dataThatBehaviours(Mockito.any())).then(filterFrom(
            Map.of(ExplorationArguments.of(1), Stream.of(ConditionOutput.PASS),
                   ExplorationArguments.of(2), Stream.of(ConditionOutput.PASS,ConditionOutput.PASS)
        )));

        try {
            exploratoryRunner.tests()
                             .forEach(t -> t.test.run());
        } catch (Exception e) {
            fail("All data passes something");
        }
    }

    private Answer<List<ExplorationArguments>> filterFrom(Map<ExplorationArguments, Stream<ConditionOutput>> data) {
        return invocationOnMock -> {
            Predicate<Stream<ConditionOutput>> predicate = invocationOnMock.getArgument(0);
            return data.entrySet()
                       .stream()
                       .filter(e -> predicate.test(e.getValue()))
                       .map(Map.Entry::getKey)
                       .collect(Collectors.toList());
        };
    }


}
