package dev.ohate.swift.feedback;

import dev.ohate.swift.Swift;
import dev.ohate.swift.payload.Payload;

import java.util.UUID;

/**
 * The FeedbackPayload class represents a payload used for feedback messaging within the Swift framework.
 * It extends the base Payload class and includes properties specific to feedback messages.
 */
public class FeedbackPayload extends Payload {

    private final UUID feedbackId;
    private FeedbackState state;
    private final int ttl;

    private transient Swift swift;

    /**
     * Creates a new FeedbackPayload instance with the specified time-to-live (TTL) duration.
     *
     * @param ttl The time-to-live duration for the feedback payload in milliseconds.
     */
    public FeedbackPayload(int ttl) {
        this.feedbackId = UUID.randomUUID();
        this.state = FeedbackState.REQUEST;
        this.ttl = ttl;
    }

    /**
     * Sets the Swift instance associated with this feedback payload.
     *
     * @param swift The Swift instance to associate with this feedback payload.
     */
    public void setSwift(Swift swift) {
        this.swift = swift;
    }

    /**
     * Sends a response for the feedback payload, transitioning its state to RESPONSE,
     * and broadcasts it via the associated Swift instance.
     */
    public void sendResponse() {
        if (state == FeedbackState.RESPONSE) {
            return;
        }

        state = FeedbackState.RESPONSE;
        swift.broadcastPayload(this);
    }

    /**
     * Gets the unique identifier for the feedback payload.
     *
     * @return The feedback payload's unique identifier.
     */
    public UUID getFeedbackId() {
        return feedbackId;
    }

    /**
     * Gets the current state of the feedback payload.
     *
     * @return The current state, which can be REQUEST or RESPONSE.
     */
    public FeedbackState getState() {
        return state;
    }

    /**
     * Gets the time-to-live (TTL) duration for the feedback payload in milliseconds.
     *
     * @return The TTL duration in milliseconds.
     */
    public int getTtl() {
        return ttl;
    }

}
