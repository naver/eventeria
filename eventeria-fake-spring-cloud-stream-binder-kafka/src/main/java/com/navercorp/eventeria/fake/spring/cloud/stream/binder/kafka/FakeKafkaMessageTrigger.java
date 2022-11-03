package com.navercorp.eventeria.fake.spring.cloud.stream.binder.kafka;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;

public final class FakeKafkaMessageTrigger {
	private final FakeKafkaChannelBinder binder;

	public FakeKafkaMessageTrigger(FakeKafkaChannelBinder binder) {
		this.binder = binder;
	}

	public void sendOutboundChannelMessage(String channelName, Object message) {
		MessageChannel channel = this.binder.getOutboundChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException(
				"channel does not registered for fake kafka binder outbound channel. channelName: " + channelName
			);
		}

		channel.send(new GenericMessage<>(message));
	}

	public void sendInboundChannelMessage(String channelName, Object message) {
		SubscribableChannel channel = this.binder.getInboundChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException(
				"channel does not registered for fake kafka binder inbound channel. channelName: " + channelName
			);
		}

		channel.send(new GenericMessage<>(message));
	}
}
