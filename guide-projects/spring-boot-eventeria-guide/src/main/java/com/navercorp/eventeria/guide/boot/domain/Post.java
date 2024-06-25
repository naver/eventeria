package com.navercorp.eventeria.guide.boot.domain;

import java.time.Instant;

import lombok.Builder;

@Builder
public record Post(
	long id,

	String writerId,

	String content,

	Instant createdAt
) {
}
