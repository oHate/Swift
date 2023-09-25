package dev.ohate.swift.payload;

import dev.ohate.swift.handler.CachedPayloadHandler;
import dev.ohate.swift.handler.PayloadHandler;

import java.lang.reflect.Method;
import java.util.*;

/**
 * The PayloadRegistry class manages the registration of payload classes and their associated handlers.
 */
public class PayloadRegistry {

    // Map to store payload classes by their IDs
    private final Map<String, Class<? extends Payload>> payloads = new HashMap<>();

    // Map to store payload handlers by payload class
    private final Map<Class<? extends Payload>, List<CachedPayloadHandler>> payloadHandlers = new HashMap<>();

    /**
     * Get the payload class associated with a given ID.
     *
     * @param id The ID of the payload.
     * @return The payload class or null if not found.
     */
    public Class<? extends Payload> getPayloadById(String id) {
        return payloads.get(id);
    }

    /**
     * Register multiple payload classes.
     *
     * @param payloads The payload classes to register.
     */
    public void registerPayloads(Class<? extends Payload>... payloads) {
        for (Class<? extends Payload> payload : payloads) {
            try {
                registerPayload(payload);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Register a payload class.
     *
     * @param payload The payload class to register.
     * @throws RuntimeException if the payload is already registered.
     */
    public void registerPayload(Class<? extends Payload> payload) {
        String payloadName = payload.getName();

        if (payloads.containsKey(payloadName)) {
            throw new RuntimeException("Payload '" + payloadName + "' is already registered.");
        }

        payloads.put(payloadName, payload);
    }

    /**
     * Invoke handlers for a given payload.
     *
     * @param payload The payload to invoke handlers for.
     */
    public void invokePayload(Payload payload) {
        Class<?> payloadClass = payload.getClass();

        if (!payloadHandlers.containsKey(payloadClass)) {
            return;
        }

        List<CachedPayloadHandler> handlers = payloadHandlers.get(payloadClass);

        if (handlers.isEmpty()) {
            return;
        }

        for (CachedPayloadHandler handler : handlers) {
            handler.executeHandler(payload);
        }
    }

    /**
     * Register a listener object containing payload handler methods.
     *
     * @param listener The listener object to register.
     */
    public void registerListener(PayloadListener listener) {
        Class<?> listenerClass = listener.getClass();

        for (Method method : listenerClass.getDeclaredMethods()) {
            if (!isHandler(method)) {
                continue;
            }

            Class<? extends Payload> payloadClass = (Class<? extends Payload>) method.getParameterTypes()[0];
            List<CachedPayloadHandler> handlers = new ArrayList<>();

            if (payloadHandlers.containsKey(payloadClass)) {
                handlers = payloadHandlers.get(payloadClass);
            }

            handlers.add(new CachedPayloadHandler(listener, method.getAnnotation(PayloadHandler.class).priority(), method));
            handlers.sort(Comparator.comparing(CachedPayloadHandler::getPriority));

            payloadHandlers.put(payloadClass, handlers);
        }
    }

    /**
     * Register multiple listener objects containing payload handler methods.
     *
     * @param listeners The listener objects to register.
     */
    public void registerListeners(PayloadListener... listeners) {
        for (PayloadListener listener : listeners) {
            registerListener(listener);
        }
    }

    /**
     * Check if a method is a valid payload handler.
     *
     * @param method The method to check.
     * @return True if the method is a valid payload handler, false otherwise.
     */
    public static boolean isHandler(Method method) {
        return method.isAnnotationPresent(PayloadHandler.class) &&
                method.getParameterTypes().length == 1 &&
                Payload.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

}
