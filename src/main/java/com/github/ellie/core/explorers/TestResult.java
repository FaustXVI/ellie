package com.github.ellie.core.explorers;

import com.github.ellie.core.ConditionOutput;
import com.github.ellie.core.ExplorationArguments;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.github.ellie.core.ConditionOutput.FAIL;
import static com.github.ellie.core.ConditionOutput.PASS;

@FunctionalInterface
public interface TestResult {
    Collection<ExplorationArguments> argumentsThat(ConditionOutput output);

    default double computeCorrelationFactorWith(TestResult secondResult) {
        Collection<ExplorationArguments> passed = argumentsThat(PASS);
        Collection<ExplorationArguments> failing = argumentsThat(FAIL);
        Collection<ExplorationArguments> precond = secondResult.argumentsThat(PASS);
        Collection<ExplorationArguments> notPrecond = secondResult.argumentsThat(FAIL);

        Collection<ExplorationArguments> passedAndPrecond = passed.stream()
                .filter(precond::contains)
                .collect(Collectors.toList());
        Collection<ExplorationArguments> failedAndPrecond = failing.stream()
                .filter(precond::contains)
                .collect(Collectors.toList());
        Collection<ExplorationArguments> passedAndNotPrecond = passed.stream()
                .filter(notPrecond::contains)
                .collect(Collectors.toList());
        Collection<ExplorationArguments> failedAndNotPrecond = failing.stream()
                .filter(notPrecond::contains)
                .collect(Collectors.toList());


        int n = passed.size() + failing.size();

        double Epre = precond.size() * 1.0 / n;
        double Epost = passed.size() * 1.0 / n;

        double standardDerivationPre = Math.sqrt(
                Math.pow(0d - Epre, 2) * notPrecond.size() +
                        Math.pow(1d - Epre, 2) * precond.size()
        );
        double standardDerivationPost = Math.sqrt(
                Math.pow(0 - Epost, 2) * failing.size() +
                        Math.pow(1 - Epost, 2) * passed.size()
        );
        ;

        double covariance =
                failedAndNotPrecond.size() * (0 - Epre) * (0 - Epost)
                        + failedAndPrecond.size() * (1 - Epre) * (0 - Epost)
                        + passedAndNotPrecond.size() * (0 - Epre) * (1 - Epost)
                        + passedAndPrecond.size() * (1 - Epre) * (1 - Epost);

        return Math.abs(covariance / (standardDerivationPost * standardDerivationPre));
    }
}
