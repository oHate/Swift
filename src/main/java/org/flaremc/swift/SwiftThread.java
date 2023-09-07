package org.flaremc.swift;

import org.flaremc.swift.util.Redis;

/**
 * The SwiftThread class represents a background thread responsible for subscribing to a Redis Pub/Sub channel.
 * It continuously listens for incoming messages and handles them using the Swift framework.
 */
public class SwiftThread extends Thread {

    private static final String THREAD_NAME = "Swift - Subscription Thread";

    private final Swift swift;
    private final SwiftPubSub pubSub;
    private final int retryDelay;

    /**
     * Create a SwiftThread instance associated with a Swift instance.
     *
     * @param swift      The Swift instance to which this SwiftThread is associated.
     * @param retryDelay The delay (in milliseconds) between reconnection attempts in case of a failure.
     */
    public SwiftThread(Swift swift, int retryDelay) {
        super(THREAD_NAME);
        this.swift = swift;
        this.pubSub = new SwiftPubSub(swift);
        this.retryDelay = Math.max(retryDelay, 1_000);
    }

    /**
     * The main execution method of the SwiftThread. It continuously subscribes to a Redis Pub/Sub channel
     * and processes incoming messages using the Swift framework.
     */
    @Override
    public void run() {
        while (true) {
            Redis redis = swift.getRedis();

            if (!redis.isConnected()) {
                redis.connect();
            }

            try {
                redis.runRedisCommand(jedis -> {
                    jedis.subscribe(pubSub, swift.getNetwork());
                    return null;
                });
            } catch (Exception e) {
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ignored) {}
            }
        }
    }

}
