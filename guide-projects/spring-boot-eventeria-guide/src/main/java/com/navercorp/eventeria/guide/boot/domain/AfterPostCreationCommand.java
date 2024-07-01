package com.navercorp.eventeria.guide.boot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.messaging.contract.command.AbstractCommand;
import com.navercorp.eventeria.messaging.contract.command.Command;

public sealed interface AfterPostCreationCommand extends Command {

	@Override
	default String getSourceType() {
		return Post.class.getName();
	}

	// functional binding, concurrent
	@Getter
	@RequiredArgsConstructor
	final class NotifyToSubscribers extends AbstractCommand implements AfterPostCreationCommand {
		private final long postId;
	}

	// programmatic binding
	@Getter
	@RequiredArgsConstructor(staticName = "of")
	final class UpdateUserStatistic extends AbstractCommand implements AfterPostCreationCommand {
		private final long postId;
		private final String writerId;
	}

	// programmatic binding
	@Getter
	@RequiredArgsConstructor(staticName = "from")
	final class RefreshPostRanking extends AbstractCommand implements AfterPostCreationCommand {
		private final long postId;
	}

	// programmatic binding
	@Getter
	@RequiredArgsConstructor(staticName = "from")
	final class ApplySearchIndex extends AbstractCommand implements AfterPostCreationCommand {
		private final long postId;
	}
}
