# Eventeria

Event Driven lightweight message format

## Version compatibility

 eventeria | spring-boot   | spring-cloud-stream 
-----------|---------------|---------------------
 1.2.2     | 3.2.x ~ 3.3.x | 4.1.x               
 1.1.1     | 3.0.x ~ 3.1.x | 4.0.x               
 1.0.1     | 2.7.x         | 3.2.x               

## Getting Started

- [Document](https://github.com/naver/eventeria/wiki)
- [spring-boot-eventeria guide projects](https://github.com/naver/eventeria/tree/main/guide-projects/spring-boot-eventeria-guide)

```gradle
dependencies {
    implementation("com.navercorp.eventeria:spring-boot-eventeria:${version}")
}
```

```java
@Configuration
public class MessageConfig {
    @Bean
    ChannelBindable channelBindable() {
        return new ChannelBindable();
    }

    @Bean
    @ConditionalOnMissingBean
    ChannelBinder channelBinder(
        SubscribableChannelBindingTargetFactory bindingTargetFactory,
        ChannelBindable channelBindable
    ) {
        return new DefaultChannelBinder(bindingTargetFactory, channelBindable);
    }
}
```

## Produce Message

1. Register your spring-cloud-stream produce channel in application.yml or application.properties
    ```yml
    spring:
      cloud:
        stream:
          bindings:
            outbound-kafka-channel-command:
              destination: command_topic
              producer:
                partition-count: 2
    ```
2. Register beans of outbound `MessageChannel`, `SpringMessagePublisher`, `IntegrationFlow`.
    - [example code](https://github.com/naver/eventeria/blob/main/guide-projects/spring-boot-eventeria-guide/src/main/java/com/navercorp/eventeria/guide/boot/publisher/ProgrammaticBindingNotifyCommandPublisher.java)
3. Inject `SpringMessagePublisher` bean and publish messages.

```java
@Service
public class ExampleService {
    ExampleService(
        @Qualified("springMessagePublisherBean") SpringMessagePublisher springMessagePublisher
    ) {
        this.springMessagePublisher = springMessagePublisher;
    }
    
    public void publish(Message message) {
        springMessagePublisher.publish(message);
    }
}
```

> **NOTE:** Does not support Functional publishing currently.

## Consume Message

1. First, register your spring-cloud-stream consume channel in application.yml or application.properties
    ```yml
    spring:
      cloud:
        stream:
          bindings:
            inbound-kafka-channel-command:
              group: example_group
              destination: command_topic
              consumer:
                concurrency: 1
    ```
2. Choose one of the Programmatic or Functional way to consume `Message`s.

### Programmatic way

- Register beans of inbound `SubscribableChannel`, `SpringMessagePublisher`, `IntegrationFlow`. `MessageRouter`
   - [example code](https://github.com/naver/eventeria/blob/main/guide-projects/spring-boot-eventeria-guide/src/main/java/com/navercorp/eventeria/guide/boot/listener/ProgrammaticBindingEventListener.java)
   - `MessageRouter`: route messages to your application.

### Functional way

1. Register additional function mappings in application.yml or application.properties.
    ```yml
    spring:
       cloud:
         function:
           definition:
             transformCloudEventToMessage|consumeImplementsEventeriaMessage;
             transformCloudEventToMessage|consumeXXX;
    ```
2. Register `Function` bean which converts spring-messaging `Message` to eventeria `Message`.
    ```java
    @Bean
    Function<org.springframework.messaging.Message<byte[]>, Message> transformCloudEventToMessage(
        CloudEventMessageReaderWriter cloudEventMessageReaderWriter
    ) {
        return FunctionalBindingSupports.convertToMessage(cloudEventMessageReaderWriter);
    }
    ```
3. Register your `Consumer` bean which receives data type of eventeria `Message`.
    ```java
    @Bean
    Consumer<ImplementsEventeriaMessage> consumeImplementsEventeriaMessage() {
        return command -> log.info("[CONSUME][consumeImplementsEventeriaMessage] {}", command);
    }
    ```

## `typealias` extension

Eventeria provides the `typealias` extension to support following features.

- multiple data formats for single topic.
- publish the string of message type in fixed string, even if rename of class or relocate package.

### Publishing

- When publishing `Message`, the message will contain `typealias` extension that you registered by `addSerializeTypeAlias` or `addCompatibleTypeAlias`. If not, full class name will be used.

### Consuming

- When consuming `Message`, eventeria try to deserialize the message with `typealias` extension. You should register typealias by `addDeserializeTypeAlias` or `addCompatibleTypeAlias  
- If eventeria failed to find deserialize target type, it try to load class using ClassLoader with provided `typealias` string. If there is no matching class, consuming will be failed.

### Configuration Example

```java
@Bean
CloudEventMessageTypeAliasMapper cloudEventMessageTypeAliasMapper() {
    CloudEventMessageTypeAliasMapper typeAliasMapper = new CloudEventMessageTypeAliasMapper();

    // support typealias at both serialization / deserialization times
    typeAliasMapper.addCompatibleTypeAlias(
        PostCreatedEvent.class,
        "com.navercorp.eventeria.guide.boot.domain.PostCreatedEvent"
    );

    // support typealias at serialization time only
    typeAliasMapper.addSerializeTypeAlias(
        SerializeOnlyTypeAliasPostCreatedEvent.class,
        "com.navercorp.eventeria.guide.boot.domain.PostCreatedEventSerializeOnlyVersion"
    );

    return typeAliasMapper;
}
```

## Message routing by class type

If typealias is configured correctly, you can also configure 'route functions' to handle message by class type.  
This feature is provided for programmatic binding only.


```java
// register router
@Bean("router")
MessagePayloadTypeRouter eventRouter(
    YourEventHandler handler
) {
    return MessagePayloadTypeRouter.register("beanname")
        .route(CreatedEvent.class, handler::onCreatedEvent)
        .route(RemovedEvent.class, handler::onRemovedEvent)
        .done();
}

// map inbound handler and router
@Bean
IntegrationFlow eventRouterIntegrationFlow(
    @Qualifier("inboundHandlerName") SpringMessageHandler springMessageHandler,
    @Qualifier("router") MessagePayloadTypeRouter router
) {
    return IntegrationFlow.from(springMessageHandler)
        .route(router)
        .get();
}
```

## Schedule message publishing time

You can schedule the time when a message is published.  
To use this feature, you should configure a persistence store to save/retrieve `TimerMessage`.

### Details

1. Publish a message that implements `TimerMessage`, with the time when should be published.
2. Published message is converted to `TimerMessageStoreValue` which is saved in `TimerMessageStore`.
3. A retriever finds messages to publish from `TimerMessageStore` periodically.
    - If exists, it will publish them to actual output channel.

### Additional dependencies

```gradle
dependencies {
    implementation("com.navercorp.eventeria:eventeria-timer-spring-integration:${version}")

    // provides `TimerMessageStoreValue` implementations using redis
    implementation("com.navercorp.eventeria:eventeria-timer-spring-redis:${version}")
    
    // If you use in-memory or other persistence store, import this and implement `TimerMessageStoreValue` for your store.
    implementation("com.navercorp.eventeria:eventeria-timer:${version}")
}
```

### Examples

You must configure beans following, `SpringTimerMessageHandler`, `Scheduler`, `IntegrationFlowAdapter`

```java
// bean example using in-memory store
@Bean
SpringTimerMessageHandler springTimerMessageHandler() {
    TimerMessageStore timerMessageStore = new InMemoryTimerMessageStore();
    TimerMessageHandler timerMessageHandler = new SimpleTimerMessageHandler(timerMessageStore, 1000);

    return new SpringTimerMessageHandler(timerMessageHandler);
}

// bean example using redis store with distributed partitions supported.
@Bean
SpringTimerMessageHandler springTimerMessageHandler(
    CloudEventMessageReaderWriter cloudEventMessageReaderWriter,
    RedisOperations<String, String> redisIndexOperations,
    RedisOperations<String, RedisCloudEventTimerMessageStoreValue> redisCloudEventTimerMessageStoreValueRedisOperations
) {
    TimerMessageStore timerMessageStore = new RedisCloudEventTimerMessageStore(
        "timer-message-", // redis key prefix
        redisIndexOperations,
        redisCloudEventTimerMessageStoreValueRedisOperations,
        cloudEventMessageReaderWriter
    );
    TimerMessageHandler timerMessageHandler = new DistributedTimerMessageHandler(
        timerMessageStore, 
        1000, // count per each release
        DefaultPartitionGenerator(),
        10, // registered partition count
        10, // seek partition count
        null // Executor
    );

    return new SpringTimerMessageHandler(timerMessageHandler);
}
```

```java
@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class SchedulingConfig {

    private final SpringTimerMessageHandler springTimerMessageHandler;

    @Scheduled(fixedDelay = 1000L)
    void triggerReschedule() {
        springTimerMessageHandler.reschedulePersistedMessages();
    }
}
```

```java
@Bean
IntegrationFlowAdapter outboundIntegrationFlowAdapter(
    MessageChannel outboundChannel,
    MessageToCloudEventConverter messageToCloudEventConverter,
    CloudEventHeaderMapper cloudEventHeaderMapper,
    SpringMessagePublisher springMessagePublisher,
    SpringTimerMessageHandler springTimerMessageHandler
) {
    return new TimerMessagePublisherIntegrationAdapter(
        springMessagePublisher,
        messageToCloudEventConverter,
        cloudEventHeaderMapper,
        outboundChannel,
        springTimerMessageHandler
    );
}
```

Now, you can use message scheduling like below.

```java
public class ScheduledMessage implements TimerMessage {
}

springMessagePublisher.publish(new ScheduledMessage());
```

## License

```
Copyright (c) 2022-present NAVER Corp.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
