package com.esl.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esl.dao.dictation.IDictationDAO;
import com.esl.model.dictation.DictationStatistics;

@Transactional
@Service("dictationStatService")
public class DictationStatService {
	private static Logger logger = LoggerFactory.getLogger(DictationStatService.class);

	// supporting class
	@Autowired private IDictationDAO dictationDAO;

	public DictationStatService() {}

	public DictationStatistics randomDictationStatistics(int maxResult) {
		final String logPrefix = "randomDictationStatistics: ";
		DictationStatistics stat = new DictationStatistics();
		Random r = new Random();
		stat.setType(DictationStatistics.Type.values()[r.nextInt(4)]);
		logger.info("{}randomed type [{}]", logPrefix, stat.getType());

		switch (stat.getType()) {
		case MostPracticed:
			stat.setDictations(dictationDAO.listMostPracticed(maxResult)); break;
		case NewCreated:
			stat.setDictations(dictationDAO.listNewCreated(maxResult)); break;
		case MostRecommended:
			stat.setDictations(dictationDAO.listMostRecommended(0, maxResult)); break;
		case LatestPracticed:
			stat.setDictations(dictationDAO.listLatestPracticed(maxResult)); break;
		}
		logger.info("{}returned dictations size [{}]", logPrefix, stat.getDictations().size());
		return stat;
	}
}
