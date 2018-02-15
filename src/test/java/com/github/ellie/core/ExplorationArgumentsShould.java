package com.github.ellie.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ExplorationArgumentsShould {

    static Stream<Arguments> arguments() {
        return Stream.of(
            Arguments.of(ExplorationArguments.of(2), "[2]"),
            Arguments.of(ExplorationArguments.of(2,3), "[2, 3]")
        );
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void transformToStringAllArguments(ExplorationArguments arguments, String toString) {
        assertThat(arguments.toString()).isEqualTo(toString);
    }
}
