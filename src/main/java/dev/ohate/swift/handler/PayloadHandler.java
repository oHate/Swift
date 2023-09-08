package dev.ohate.swift.handler;

import dev.ohate.swift.payload.PayloadPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark methods as payload handlers for payload listeners.
 * Payload handlers are methods within payload listener classes that are responsible for handling specific payloads.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PayloadHandler {

    /**
     * The priority of the payload handler.
     *
     * @return The priority level for the handler (default is PayloadPriority.NORMAL).
     */
    PayloadPriority priority() default PayloadPriority.NORMAL;

}
