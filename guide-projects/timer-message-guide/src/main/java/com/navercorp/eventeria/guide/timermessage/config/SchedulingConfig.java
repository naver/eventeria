package com.navercorp.eventeria.guide.timermessage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.timer.spring.integration.handler.SpringTimerMessageHandler;

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
