package com.navercorp.eventeria.guide.boot.domain;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.guide.boot.publisher.ProgrammaticBindingEventPublisher;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessagePublisher;

@Service
@RequiredArgsConstructor
public class CreatePostService {

	@Qualifier(ProgrammaticBindingEventPublisher.OUTBOUND_BEAN_NAME)
	private final SpringMessagePublisher springMessagePublisher;

	/**
	 * publish {@link PostCreatedEvent} and return request parameter.
	 *
	 * @param post
	 * @return
	 */
	public Post create(Post post) {
		springMessagePublisher.publish(
			PostCreatedEvent.from(post)
		);

		springMessagePublisher.publish(
			SerializeOnlyTypeAliasPostCreatedEvent.from(post)
		);

		return post;
	}
}
