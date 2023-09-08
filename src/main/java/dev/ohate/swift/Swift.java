package dev.ohate.swift;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.ohate.swift.feedback.Feedback;
import dev.ohate.swift.feedback.FeedbackHandler;
import dev.ohate.swift.feedback.FeedbackPayload;
import dev.ohate.swift.payload.Payload;
import dev.ohate.swift.payload.PayloadRegistry;
import dev.ohate.swift.util.Redis;

import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * The Swift class represents a messaging framework for broadcasting payloads across a network.
 */
public class Swift {

    public static final Gson GSON = new Gson();

    private final String network;
    private final String unit;
    private final PayloadRegistry registry;
    private final FeedbackHandler feedbackHandler;

    private Redis redis;

    /**
     * Create a Swift instance with default Redis timeout and Swift retry delay.
     *
     * @param network  The name of the network.
     * @param unit     The unit or server name.
     * @param redisUri The URI of the Redis server.
     */
    public Swift(String network, String unit, String redisUri) {
        this(network, unit, redisUri, 5_000, 10_000);
    }

    /**
     * Create a Swift instance with custom Redis timeout and Swift retry delay.
     *
     * @param network         The name of the network.
     * @param unit            The unit or server name.
     * @param redisUri        The URI of the Redis server.
     * @param redisTimeOut    The connection timeout for Redis in milliseconds.
     * @param swiftRetryDelay The delay in milliseconds between retries for the Swift thread.
     */
    public Swift(String network, String unit, String redisUri, int redisTimeOut, int swiftRetryDelay) {
        this.network = network;
        this.unit = unit;
        this.registry = new PayloadRegistry();
        this.feedbackHandler = new FeedbackHandler();

        Redis redisInstance;

        try {
            redisInstance = new Redis(redisUri, redisTimeOut);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        this.redis = redisInstance;
        this.redis.connect();

        new SwiftThread(this, swiftRetryDelay).start();
    }

    /**
     * Broadcast a payload to the network.
     *
     * @param payload The payload to broadcast.
     */
    public void broadcastPayload(Payload payload) {
        redis.runRedisCommand(jedis -> jedis.publish(network, getTransmitReadyData(payload)));
    }

    /**
     * Broadcasts a feedback payload to the network and associates it with a callback for handling responses.
     *
     * @param payload  The feedback payload to broadcast.
     * @param callback A consumer function that handles responses to the feedback payload.
     * @param <T>      The type of the feedback payload.
     */
    public <T extends FeedbackPayload> void broadcastFeedbackPayload(T payload, Consumer<T> callback) {
        feedbackHandler.addFeedback(new Feedback<>(payload.getFeedbackId(), payload.getTtl(), callback));
        redis.runRedisCommand(jedis -> jedis.publish(network, getTransmitReadyData(payload)));
    }

    /**
     * Prepares a payload for transmission by serializing it into JSON and adding origin information.
     *
     * @param payload The payload to prepare.
     * @return A formatted string ready for transmission.
     */
    private String getTransmitReadyData(Payload payload) {
        JsonObject json = GSON.toJsonTree(payload).getAsJsonObject();
        json.addProperty("origin", unit);

        return payload.getClass().getName() + "&" + json;
    }

    /**
     * Get the Redis instance associated with this Swift instance.
     *
     * @return The Redis instance.
     */
    public Redis getRedis() {
        return redis;
    }

    /**
     * Get the name of the network associated with this Swift instance.
     *
     * @return The network name.
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Get the unit or server name associated with this Swift instance.
     *
     * @return The unit or server name.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Get the payload registry associated with this Swift instance.
     *
     * @return The payload registry.
     */
    public PayloadRegistry getRegistry() {
        return registry;
    }

    /**
     * Get the feedback handler associated with this Swift instance.
     *
     * @return The feedback handler.
     */
    public FeedbackHandler getFeedbackHandler() {
        return feedbackHandler;
    }

}
