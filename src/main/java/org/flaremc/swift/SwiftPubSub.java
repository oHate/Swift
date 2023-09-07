package org.flaremc.swift;

import org.flaremc.swift.payload.Payload;
import org.flaremc.swift.payload.PayloadRegistry;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;

/**
 * The SwiftPubSub class is responsible for handling messages received from a Redis Pub/Sub channel.
 * It decodes and dispatches payloads to the appropriate handlers in the Swift framework.
 */
public class SwiftPubSub extends JedisPubSub {

    private final Swift swift;

    /**
     * Create a SwiftPubSub instance associated with a Swift instance.
     *
     * @param swift The Swift instance to which this SwiftPubSub instance is associated.
     */
    public SwiftPubSub(Swift swift) {
        this.swift = swift;
    }

    /**
     * Callback method invoked when a message is received on the subscribed channel.
     *
     * @param channel The name of the channel where the message was received.
     * @param message The message content.
     */
    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equals(swift.getNetwork())) {
            return;
        }

        String[] data = getDataFromBody(message);
        String payloadId = data[0];
        String payloadData = data[1];

        PayloadRegistry registry = swift.getRegistry();
        Class<? extends Payload> payloadClass = registry.getPayloadById(payloadId);

        if (payloadClass == null) {
            return;
        }

        Payload payload = Swift.GSON.fromJson(payloadData, payloadClass);

        if(!payload.sendToSelf() && swift.getUnit().equals(payload.getOrigin())) {
            return;
        }

        registry.invokePayload(payload);
    }

    /**
     * Split a message string into payload ID and payload data parts.
     *
     * @param body The message content to split.
     * @return An array containing payload ID as the first element and payload data as the second element.
     */
    private String[] getDataFromBody(String body) {
        String[] fragments = body.split("&");

        String payloadId = fragments[0];
        String payloadData = String.join("&", Arrays.copyOfRange(fragments, 1, fragments.length));

        return new String[]{payloadId, payloadData};
    }

}
