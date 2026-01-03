package dev.ohate.swift;

import dev.ohate.swift.handler.ListenerInvocation;
import dev.ohate.swift.handler.PayloadHandler;
import dev.ohate.swift.json.JsonProvider;
import dev.ohate.swift.payload.PayloadListener;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Swift implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Swift.class);

    private final Map<String, Class<?>> payloadTypes = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<ListenerInvocation>> payloadHandlers = new ConcurrentHashMap<>();

    private final String network;
    private final String unit;
    private final JsonProvider jsonProvider;
    private final StatefulRedisPubSubConnection<String, String> connection;

    public Swift(String network, String unit, RedisClient client, JsonProvider jsonProvider) {
        this.network = network;
        this.unit = unit;
        this.jsonProvider = jsonProvider;
        this.connection = client.connectPubSub();

        this.connection.addListener(new SwiftPubSubListener(this, jsonProvider));
        this.connection.sync().subscribe(network);
    }

    public void registerListener(PayloadListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                LOGGER.error(
                        "@PayloadHandler method must be public: {}#{}",
                        method.getDeclaringClass().getName(),
                        method.getName()
                );
                continue;
            }

            PayloadHandler annotation = method.getAnnotation(PayloadHandler.class);

            if (annotation == null || method.getParameterTypes().length != 1) {
                continue;
            }

            Class<?> payloadClass = method.getParameterTypes()[0];

            this.payloadTypes
                    .putIfAbsent(payloadClass.getName(), payloadClass);

            this.payloadHandlers
                    .computeIfAbsent(payloadClass, k -> new CopyOnWriteArrayList<>())
                    .add(new ListenerInvocation(annotation.priority(), listener, method));

            this.payloadHandlers.get(payloadClass)
                    .sort(Comparator.comparing(ListenerInvocation::getPriority));
        }
    }

    public void registerListeners(PayloadListener... listeners) {
        for (PayloadListener listener : listeners) {
            registerListener(listener);
        }
    }

    public void invokePayload(Object payload) {
        Class<?> payloadClass = payload.getClass();

        if (!this.payloadHandlers.containsKey(payloadClass)) {
            return;
        }

        List<ListenerInvocation> handlers = this.payloadHandlers.get(payloadClass);

        if (handlers.isEmpty()) {
            return;
        }

        for (ListenerInvocation handler : handlers) {
            try {
                handler.execute(payload);
            } catch (InvocationTargetException | IllegalAccessException e) {
                LOGGER.error("Failed to invoke payload handler", e);
            }
        }
    }

    public void broadcastPayload(Object payload) {
        Message message = new Message(
                payload.getClass().getName(),
                this.unit,
                this.jsonProvider.toJson(payload)
        );

        this.connection.async().publish(this.network, this.jsonProvider.toJson(message));
    }

    public Class<?> getPayloadClass(String type) {
        return this.payloadTypes.get(type);
    }

    public String getNetwork() {
        return this.network;
    }

    public String getUnit() {
        return this.unit;
    }

    @Override
    public void close() {
        this.connection.close();
    }

}
