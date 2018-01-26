package com.github.ellie;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

class AccessibleMethod {
    private final Method method;

    AccessibleMethod(Method method) {
        this.method = method;
    }

    public Object invoke(Object instance, Object... arguments) {
        method.setAccessible(true);
        try {
            return method.invoke(instance, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public String name() {
        return method.getName();
    }

    public boolean returnsAnyOf(Class<?>... classes) {
        return Arrays.stream(classes).anyMatch(c->c.isAssignableFrom(method.getReturnType()));
    }
}
