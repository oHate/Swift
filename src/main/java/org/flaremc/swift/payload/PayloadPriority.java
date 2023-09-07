package org.flaremc.swift.payload;

/**
 * Enumeration representing different priority levels for payload handlers.
 * Handlers with higher priority values are executed first.
 */
public enum PayloadPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST;

    /**
     * Get the numeric priority value of the enum constant based on its ordinal position.
     *
     * @return The priority value as an integer.
     */
    public int getPriority() {
        return ordinal();
    }

}
