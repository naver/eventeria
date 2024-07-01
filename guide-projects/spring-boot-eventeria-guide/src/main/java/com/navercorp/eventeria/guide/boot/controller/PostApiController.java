package com.navercorp.eventeria.guide.boot.controller;

import java.time.Instant;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.guide.boot.domain.CreatePostService;
import com.navercorp.eventeria.guide.boot.domain.Post;

@RestController
@RequiredArgsConstructor
public class PostApiController {

	private final CreatePostService createPostService;

	@PostMapping("/posts/{postId}")
	public PostResponse createPost(
		@PathVariable long postId,
		@RequestBody CreatePostRequest request
	) {
		Post post = createPostService.create(
			Post.builder()
				.id(postId)
				.writerId(request.writerId())
				.content(request.content())
				.createdAt(Instant.now())
				.build()
		);

		return PostResponse.builder()
			.id(post.id())
			.writerId(post.writerId())
			.content(post.content())
			.createdAt(post.createdAt())
			.build();
	}

	public record CreatePostRequest(
		long id,

		String writerId,

		String content
	) {
	}

	@Builder
	public record PostResponse(
		long id,

		String writerId,

		String content,

		Instant createdAt
	) {
	}
}
