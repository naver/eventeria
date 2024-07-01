package com.navercorp.eventeria.guide.boot.domain;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.ApplySearchIndex;
import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.RefreshPostRanking;
import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.UpdateUserStatistic;

@Slf4j
@Component
@RequiredArgsConstructor
public class AfterPostCommandHandler {

	public void updateUserStatistic(UpdateUserStatistic command) {
		log.info("[CONSUME][UpdateUserStatistic] {}", command);
	}

	public void refreshPostRanking(RefreshPostRanking command) {
		log.info("[CONSUME][RefreshPostRanking] {}", command);
	}

	public void applySearchIndex(ApplySearchIndex command) {
		log.info("[CONSUME][ApplySearchIndex] {}", command);
	}
}
