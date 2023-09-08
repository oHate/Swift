package dev.ohate.swift.feedback;

/**
 * The FeedbackState enum represents the possible states of feedback messages.
 * Feedback messages can be in the REQUEST state when initially sent and in the RESPONSE state
 * when a response is received.
 */
public enum FeedbackState {
    REQUEST,
    RESPONSE
}