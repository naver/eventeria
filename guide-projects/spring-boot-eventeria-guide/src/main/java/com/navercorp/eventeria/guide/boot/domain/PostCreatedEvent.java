package com.navercorp.eventeria.guide.boot.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.navercorp.eventeria.messaging.contract.event.AbstractDomainEvent;

@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCreatedEvent extends AbstractDomainEvent {

	private long postId;
	private String writerId;

	public PostCreatedEvent(long postId, String writerId) {
		super(String.valueOf(postId));
		this.postId = postId;
		this.writerId = writerId;
	}

	public static PostCreatedEvent from(Post post) {
		return new PostCreatedEvent(post.id(), post.writerId());
	}

	@Override
	public String getSourceType() {
		return Post.class.getName();
	}
}
