package com.github.ellie.core.explorers;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class Correlations {
    private Collection<Correlation> correlations;

    public Correlations() {
        this(Collections.emptyList());
    }

    public Correlations(Collection<Correlation> correlations) {
        this.correlations = correlations;
    }

    public void forEach(Consumer<Correlation> consumer) {
        correlations.forEach(consumer);
    }
}
