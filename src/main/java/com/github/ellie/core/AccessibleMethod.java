package com.github.ellie.core;


import org.opentest4j.TestAbortedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

class AccessibleMethod {
    private final Method method;
    private final Object instance;

    AccessibleMethod(Object instance, Method method) {
        this.method = method;
        this.instance = instance;
    }

    <T> T invoke() {
        return invoke(ExplorationArguments.of());
    }

    <T> T invoke(ExplorationArguments explorationArguments) {
        method.setAccessible(true);
        try {
            return (T) method.invoke(instance, explorationArguments.get());
        } catch (IllegalAccessException | InvocationTargetException e) {
            if(e.getCause() instanceof TestAbortedException){
                throw (TestAbortedException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    public String name() {
        return method.getName();
    }

    boolean returnsAnyOf(Class<?>... classes) {
        return Arrays.stream(classes)
                     .anyMatch(c -> c.isAssignableFrom(method.getReturnType()));
    }
}
