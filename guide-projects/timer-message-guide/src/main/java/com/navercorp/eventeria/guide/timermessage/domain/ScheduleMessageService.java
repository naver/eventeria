package com.navercorp.eventeria.guide.timermessage.domain;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.guide.timermessage.publisher.ScheduledMessagePublisher;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessagePublisher;

@Service
@RequiredArgsConstructor
public class ScheduleMessageService {

	@Qualifier(ScheduledMessagePublisher.OUTBOUND_BEAN_NAME)
	private final SpringMessagePublisher springMessagePublisher;

	/**
	 * publish {@link ScheduledMessage}.
	 */
	public void schedule(int seconds) {
		springMessagePublisher.publish(
			new ScheduledMessage(
				Instant.now().plusSeconds(seconds)
			)
		);
	}
}
