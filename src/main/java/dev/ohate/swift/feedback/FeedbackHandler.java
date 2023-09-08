package dev.ohate.swift.feedback;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The FeedbackHandler class manages feedback messages within the Swift framework.
 * It provides methods for adding, retrieving, executing, and removing feedback messages.
 */
public class FeedbackHandler {

    private final Map<UUID, Feedback> feedbackMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    /**
     * Retrieves a feedback message by its unique identifier.
     *
     * @param feedbackId The unique identifier of the feedback message to retrieve.
     * @return The feedback message with the specified identifier, or null if not found.
     */
    public Feedback getFeedback(UUID feedbackId) {
        return feedbackMap.get(feedbackId);
    }

    /**
     * Adds a feedback message to the handler and schedules its removal after it expires.
     *
     * @param feedback The feedback message to add.
     */
    public void addFeedback(Feedback feedback) {
        UUID feedbackId = feedback.getFeedbackId();

        feedbackMap.put(feedbackId, feedback);
        executor.schedule(() -> {
            removeFeedback(feedbackId);
        }, feedback.getExpiresAt() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Executes a feedback payload by its unique identifier, invoking its associated callback function.
     * If the feedback has expired or does not exist, no action is taken.
     *
     * @param feedbackPayload The feedback payload to execute.
     * @param <T>             The type of the feedback payload.
     */
    public <T extends FeedbackPayload> void executeFeedbackPayload(T feedbackPayload) {
        UUID feedbackId = feedbackPayload.getFeedbackId();

        if (!feedbackMap.containsKey(feedbackId)) {
            return;
        }

        Feedback<T> feedback = feedbackMap.get(feedbackId);

        if (feedback.hasExpired()) {
            return;
        }

        feedback.getCallback().accept(feedbackPayload);
    }

    /**
     * Removes a feedback message from the handler by its unique identifier.
     *
     * @param feedbackId The unique identifier of the feedback message to remove.
     * @return The removed feedback message, or null if not found.
     */
    public Feedback removeFeedback(UUID feedbackId) {
        return feedbackMap.remove(feedbackId);
    }

}