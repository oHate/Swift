package org.flaremc.swift.handler;

import org.flaremc.swift.payload.Payload;
import org.flaremc.swift.payload.PayloadListener;
import org.flaremc.swift.payload.PayloadPriority;

import java.lang.reflect.Method;

/**
 * Represents a cached payload handler associated with a payload listener.
 * Cached payload handlers store information about payload handling methods, including priority.
 */
public class CachedPayloadHandler {

    private final PayloadListener listener;
    private final PayloadPriority priority;
    private final Method method;

    /**
     * Create a new cached payload handler.
     *
     * @param listener The payload listener associated with this handler.
     * @param priority The priority of the handler.
     * @param method   The payload handling method.
     */
    public CachedPayloadHandler(PayloadListener listener, PayloadPriority priority, Method method) {
        this.listener = listener;
        this.priority = priority;
        this.method = method;
    }

    /**
     * Get the payload listener associated with this handler.
     *
     * @return The payload listener.
     */
    public PayloadListener getListener() {
        return listener;
    }

    /**
     * Get the priority of this cached payload handler.
     *
     * @return The priority level of the handler.
     */
    public PayloadPriority getPriority() {
        return priority;
    }

    /**
     * Get the payload handling method associated with this handler.
     *
     * @return The payload handling method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Execute the payload handling method associated with this handler.
     *
     * @param payload The payload object to be handled.
     */
    public void executeHandler(Payload payload) {
        try {
            method.invoke(listener, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
