package dev.ohate.swift;

import dev.ohate.swift.json.JsonProvider;
import io.lettuce.core.pubsub.RedisPubSubListener;

public class SwiftPubSubListener implements RedisPubSubListener<String, String> {

    private final Swift swift;
    private final JsonProvider jsonProvider;

    public SwiftPubSubListener(Swift swift, JsonProvider jsonProvider) {
        this.swift = swift;
        this.jsonProvider = jsonProvider;
    }

    @Override
    public void message(String channel, String json) {
        if (!channel.equals(this.swift.getNetwork())) {
            return;
        }

        Message message = this.jsonProvider.fromJson(json, Message.class);

        if (this.swift.getUnit().equals(message.origin())) {
            return;
        }

        Class<?> payloadClass = this.swift.getPayloadClass(message.type());

        if (payloadClass == null) {
            return;
        }

        Object payload = this.jsonProvider.fromJson(message.data(), payloadClass);

        this.swift.invokePayload(payload);
    }

    @Override
    public void message(String pattern, String channel, String message) {}

    @Override
    public void subscribed(String channel, long count) {}

    @Override
    public void psubscribed(String pattern, long count) {}

    @Override
    public void unsubscribed(String channel, long count) {}

    @Override
    public void punsubscribed(String pattern, long count) {}

}
