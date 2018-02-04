package com.esl.service;

import com.esl.dao.dictation.IDictationDAO;
import com.esl.entity.dictation.Dictation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.MissingResourceException;

@Transactional
@Service
public class DictationService {
	private static Logger logger = LoggerFactory.getLogger(DictationService.class);

	@Resource private IDictationDAO dictationDAO;

	public DictationService() {}

	public Dictation recommendDictation(long id) {
		Dictation dictation = dictationDAO.get(id);
		if (dictation == null) throw new MissingResourceException("Cannot find dictation", "Dictation", String.valueOf(id));

		dictation.setTotalRecommended(dictation.getTotalRecommended() + 1);
		dictationDAO.persist(dictation);
		return dictation;
	}
}
