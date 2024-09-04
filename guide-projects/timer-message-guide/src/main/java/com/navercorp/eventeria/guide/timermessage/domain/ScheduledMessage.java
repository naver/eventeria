package com.navercorp.eventeria.guide.timermessage.domain;

import java.time.Instant;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.navercorp.eventeria.messaging.contract.command.AbstractCommand;
import com.navercorp.eventeria.timer.contract.TimerMessage;

@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledMessage extends AbstractCommand implements TimerMessage {

	Instant scheduledAt;

	public ScheduledMessage(Instant scheduledAt) {
		this.scheduledAt = scheduledAt;
	}

	@Override
	public String getSourceType() {
		return Object.class.getName();
	}

	@Override
	public Optional<Instant> timerTime() {
		return Optional.of(scheduledAt);
	}
}
