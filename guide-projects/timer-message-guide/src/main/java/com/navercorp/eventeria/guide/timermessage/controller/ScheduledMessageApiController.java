package com.navercorp.eventeria.guide.timermessage.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.guide.timermessage.domain.ScheduleMessageService;

@RestController
@RequiredArgsConstructor
public class ScheduledMessageApiController {

	private final ScheduleMessageService scheduleMessageService;

	@PostMapping
	public void scheduleMessage(
		@RequestParam int seconds
	) {
		scheduleMessageService.schedule(seconds);
	}
}
