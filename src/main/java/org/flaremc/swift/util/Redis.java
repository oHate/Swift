package org.flaremc.swift.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

/**
 * A class for managing Redis connections and executing Redis commands.
 */
public class Redis implements Closeable {

    private final int timeout;
    private final URI redisUri;

    private JedisPool jedisPool = null;

    /**
     * Create a Redis instance with the default timeout.
     *
     * @param redisUri The URI of the Redis server.
     * @throws URISyntaxException if the URI is invalid.
     */
    public Redis(String redisUri) throws URISyntaxException {
        this(redisUri, 5_000);
    }

    /**
     * Create a Redis instance with a custom timeout.
     *
     * @param redisUri The URI of the Redis server.
     * @param timeout  The connection timeout in milliseconds.
     * @throws URISyntaxException if the URI is invalid.
     */
    public Redis(String redisUri, int timeout) throws URISyntaxException {
        this.redisUri = new URI(redisUri);
        this.timeout = Math.max(timeout, 1_000);
    }

    /**
     * Check if the Redis connection is established and not closed.
     *
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        return jedisPool != null && !jedisPool.isClosed();
    }

    /**
     * Establish a connection to the Redis server.
     */
    public void connect() {
        jedisPool = new JedisPool(redisUri, timeout);
    }

    @Override
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }

    /**
     * Execute a Redis command using a pooled Jedis resource and return the result.
     *
     * @param lambda The function that represents the Redis command.
     * @param <T>    The return type of the Redis command.
     * @return The result of the Redis command.
     * @throws IllegalStateException if a connection couldn't be established or has been forcefully closed.
     * @throws RuntimeException     if an error occurs while executing the Redis command.
     */
    public <T> T runRedisCommand(Function<Jedis, T> lambda) {
        if (jedisPool == null || jedisPool.isClosed()) {
            throw new IllegalStateException("A connection to the redis server couldn't be established or has been forcefully closed");
        }

        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            return lambda.apply(jedis);
        } catch (Exception e) {
            throw new RuntimeException("Could not use resource and return", e);
        } finally {
            if (jedis != null && jedis.isConnected()) {
                jedis.close();
            }
        }
    }

}
