package com.navercorp.eventeria.guide.boot.domain;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.ApplySearchIndex;
import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.RefreshPostRanking;
import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.UpdateUserStatistic;
import com.navercorp.eventeria.guide.boot.publisher.ProgrammaticBindingAfterPostCommandPublisher;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessagePublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventHandler {

	@Qualifier(ProgrammaticBindingAfterPostCommandPublisher.OUTBOUND_BEAN_NAME)
	private final SpringMessagePublisher springMessagePublisher;

	public void publishAfterPostCreatedCommands(
		PostCreatedEvent event
	) {
		log.info("[CONSUME][Programmatic][PostCreatedEvent] {}", event);

		springMessagePublisher.publish(
			List.of(
				UpdateUserStatistic.from(event.getPostId(), event.getWriterId()),
				RefreshPostRanking.from(event.getPostId()),
				ApplySearchIndex.from(event.getPostId())
			)
		);
	}
}
