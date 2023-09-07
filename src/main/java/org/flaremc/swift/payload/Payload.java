package org.flaremc.swift.payload;

/**
 * Base class for payload objects that can be sent between servers or systems.
 */
public class Payload {

    private String origin;

    /**
     * Determine whether the payload should be sent to the originating server.
     *
     * @return true if the payload should be sent to the originating server, false otherwise.
     */
    public boolean sendToSelf() {
        return false;
    }

    /**
     * Get the origin name of the payload.
     *
     * @return The origin of the payload.
     */
    public String getOrigin() {
        return origin;
    }

}
