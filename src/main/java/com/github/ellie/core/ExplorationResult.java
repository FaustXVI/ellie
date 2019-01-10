package com.github.ellie.core;

import java.util.Optional;

public class ExplorationResult {

    public final Optional<ErrorMessage> error;
    public final TestResult testResult;

    public ExplorationResult(TestResult testResult){
        this.testResult = testResult;
        error = Optional.empty();
    }

    public ExplorationResult(ErrorMessage error, TestResult testResult) {
        this.error = Optional.ofNullable(error);
        this.testResult = testResult;
    }
}
