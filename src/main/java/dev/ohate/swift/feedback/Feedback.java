package dev.ohate.swift.feedback;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The Feedback class represents a feedback object within the Swift framework.
 * It is used to manage feedback messages, including their unique identifiers,
 * expiration times, and associated consumers for processing responses.
 *
 * @param <T> The type of the feedback payload associated with this feedback.
 */
public class Feedback<T extends FeedbackPayload> {

    private final UUID feedbackId;
    private final long expiresAt;
    private final Consumer<T> callback;
    private final Set<String> respondedUnits = new HashSet<>();

    /**
     * Creates a new Feedback instance with the specified parameters.
     *
     * @param feedbackId The unique identifier for the feedback.
     * @param ttl        The time-to-live (TTL) duration for the feedback in milliseconds.
     * @param callback   The consumer function for handling responses to the feedback.
     */
    public Feedback(UUID feedbackId, int ttl, Consumer<T> callback) {
        this.feedbackId = feedbackId;
        this.expiresAt = System.currentTimeMillis() + ttl;
        this.callback = callback;
    }

    /**
     * Checks if the feedback has expired.
     *
     * @return true if the feedback has expired, false otherwise.
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    /**
     * Checks if a specific unit has already responded to this feedback.
     *
     * @param unit The unit name to check.
     * @return true if the unit has responded, false otherwise.
     */
    public boolean hasResponded(String unit) {
        return respondedUnits.contains(unit.toLowerCase());
    }

    /**
     * Adds a unit to the set of units that have responded to this feedback.
     *
     * @param unit The unit name to add to the list of respondents.
     */
    public void addResponse(String unit) {
        respondedUnits.add(unit.toLowerCase());
    }

    /**
     * Gets the unique identifier for the feedback.
     *
     * @return The feedback's unique identifier.
     */
    public UUID getFeedbackId() {
        return feedbackId;
    }

    /**
     * Gets the expiration timestamp (in milliseconds) for the feedback.
     *
     * @return The expiration timestamp.
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the consumer function responsible for processing responses to the feedback.
     *
     * @return The feedback's response consumer.
     */
    public Consumer<T> getCallback() {
        return callback;
    }

}
