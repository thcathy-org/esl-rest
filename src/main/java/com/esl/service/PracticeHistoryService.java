package com.esl.service;

import com.esl.dao.repository.PracticeHistoryRepository;
import com.esl.entity.practice.PracticeHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class PracticeHistoryService {
	private static Logger log = LoggerFactory.getLogger(PracticeHistoryService.class);

	@Autowired private PracticeHistoryRepository practiceHistoryRepository;

	@Value("${PracticeHistory.MaxPerMember}")
	private int maxHistoryPerMember;

	public PracticeHistoryService() {}

	public PracticeHistory saveNewHistory(PracticeHistory history) {
		var oldHistories = practiceHistoryRepository.findByMember(history.getMember(), Sort.by("createdDate")).join();
		if (oldHistories.size() >= maxHistoryPerMember)
			practiceHistoryRepository.delete(oldHistories.get(0));
		return practiceHistoryRepository.save(history);
	}
}
