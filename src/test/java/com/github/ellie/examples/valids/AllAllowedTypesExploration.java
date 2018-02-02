package com.github.ellie.examples.valids;

import com.github.ellie.api.DataProvider;
import com.github.ellie.api.PotentialBehaviour;
import com.github.ellie.api.TestedBehaviour;
import com.github.ellie.core.ExplorationArguments;
import com.github.ellie.junit5.ExploratoryTest;
import org.assertj.core.api.Assertions;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AllAllowedTypesExploration implements ExploratoryTest {

    @DataProvider
    public Collection<ExplorationArguments> two() {
        return List.of(ExplorationArguments.of(2, 4));
    }

    @DataProvider
    public Stream<ExplorationArguments> four() {
        return Stream.of(ExplorationArguments.of(20, 40));
    }

    @TestedBehaviour
    public int times2(int a, int b) {
        return a + b;
    }

    @PotentialBehaviour
    public Predicate<Integer> isLessThan10(int a, int b) {
        return i -> i < 10;
    }

    @PotentialBehaviour
    public Consumer<Integer> isMoreThan10(int a, int b) {
        return i -> Assertions.assertThat(i)
                              .isGreaterThan(10);
    }
}
