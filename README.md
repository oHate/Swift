# Swift

[![](https://jitpack.io/v/oHate/Swift.svg)](https://jitpack.io/#ohate/swift)[![Release](https://img.shields.io/github/release/ohate/swift.svg?sort=semver)](https://github.com/ohate/swift/releases/latest)

## What is Swift?

Swift is a [Jedis](https://github.com/redis/jedis) Pub/Sub handler that makes it easier to broadcast messages (called payloads) to all subscribers.

## Getting started

To get started with Swift, first add the JitPack repository. If you're using Maven, that looks like this:

```xml
<repository>  
    <id>jitpack.io</id>  
    <url>https://jitpack.io</url>  
</repository>
```

Next you will need to add Swift as a dependency. Replace **LATEST** with the latest GitHub release number.

```xml
<dependency>
    <groupId>dev.ohate</groupId>
    <artifactId>Swift</artifactId>
    <version>LATEST</version>
</dependency>
```

## How do I use Swift?

First create an instance of Swift.

```java
Swift swift = new Swift("networkName", "unitName", "redisUri");
```

`networkName` - The name of the network that payloads will be broadcasted on.

`unitName` - The name of the application or unit that broadcasted the payload, this field is used as the origin name in a payload.

`redisUri` - The uri that will be used to connect to Redis, for example `redis://127.0.0.1:6379/0`

Next you will need to create a Payload. We use Gson to serialize the Payload class as Json.

```java
public class UserJoinPayload extends Payload {  
  
    private final String username;  
  
    public UserJoinPayload(String username) {  
        this.username = username;  
    }  
  
    public String getUsername() {  
        return username;  
    }  
      
}
```

In order to do anything useful when we receive the payload we need to created a listener. Payload listeners implement the PayloadListener interface, every payload handler is annotated with the @PayloadHandler annotation.

```java
public class TestPayloadListener implements PayloadListener {  
  
    @PayloadHandler  
    public void onUserJoin(UserJoinPayload payload) {  
        System.out.println(payload.getUsername() + " has joined!");  
    }  
  
}
```

To receive and send these payloads you will need to register the two objects we created.

```java
PayloadRegistry registry = swift.getRegistry();  
  
registry.registerPayloads(UserJoinPayload.class);  

registry.registerListener(new TestPayloadListener());
```

To send the payload you can broadcast it by using the broadcast method in Swift.

```java
swift.broadcastPayload(new UserJoinPayload("John"));
```

## Contributing

I love your contributions! Bug reports are always welcome! [You can open a bug report on GitHub](https://github.com/ohate/swift/issues/new).
