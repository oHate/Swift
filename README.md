# Swift

**Swift** provides a simple way to broadcast typed payloads across a Redis Pub/Sub network and dispatch them to annotated listeners with priority ordering. 
Swift interacts with Redis using the [Lettuce](https://github.com/redis/lettuce) library.

## Core Concepts

### Payload

A **payload** is any Java object you want to send across the network.

```java
public class MessagePayload {
    private final String message;
}
```

### Payload Listener

A **listener** is a class that implements `PayloadListener` and contains methods annotated with `@PayloadHandler`

```java
public class ChatListener implements PayloadListener {

    @PayloadHandler(priority = PayloadPriority.NORMAL)
    public void onChat(ChatMessagePayload payload) {
        System.out.println(payload.getMessage());
    }
}
```

Each handler method:
- Must be annotated with `@PayloadHandler`
- Must take **exactly one parameter**
- That parameter defines the payload type it handles

### Payload Priority

Handlers are executed from `HIGHEST` to `LOWEST`.

```java
public enum PayloadPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST
}
```

## Usage

### Creating a Swift instance

```java
RedisClient redisClient = RedisClient.create("redis://localhost:6379");
JsonProvider jsonProvider = new GsonJsonProvider();

Swift swift = new Swift(
    "my-network",   // Redis channel (network)
    "unit-1",       // Sender identifier (unit)
    redisClient,
    jsonProvider
);
```

`network` - Redis Pub/Sub channel that payloads will be broadcast on.

`unit` - Identifier of the payload sender.

### Registering listeners

```java
swift.registerListener(new ChatMessageListener());
```

Or multiple at once:

```java
swift.registerListeners(
    new ChatMessageListener(),
    new UserConnectionListener()
);
```

Listeners can be registered at runtime and are thread-safe.

### Broadcasting a payload

```java
swift.broadcastPayload(new ChatMessagePayload("Hello world"));
```

This:
1. Serializes the payload using the provided `JsonProvider`.
2. Wraps it in a `Message` object.
3. Publishes it to Redis.