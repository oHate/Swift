package dev.ohate.swift.handler;

import dev.ohate.swift.payload.PayloadListener;
import dev.ohate.swift.payload.PayloadPriority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Handler {

    private final PayloadPriority priority;
    private final PayloadListener listener;
    private final Method method;

    public Handler(PayloadPriority priority, PayloadListener listener, Method method) {
        this.priority = priority;
        this.listener = listener;
        this.method = method;
    }

    public PayloadPriority getPriority() {
        return priority;
    }

    public void execute(Object payload) throws InvocationTargetException, IllegalAccessException {
        this.method.invoke(this.listener, payload);
    }

}
