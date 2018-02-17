package com.esl.service;

import com.esl.dao.dictation.DictationHistoryDAO;
import com.esl.dao.dictation.IDictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationHistory;
import com.esl.entity.dictation.Vocab;
import com.esl.entity.rest.CreateDictationHistoryRequest;
import com.esl.entity.rest.CreateDictationRequest;
import com.esl.entity.rest.VocabPracticeHistory;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

@Transactional
@Service
public class DictationService {
	private static Logger logger = LoggerFactory.getLogger(DictationService.class);

	@Resource private IDictationDAO dictationDAO;
	@Resource private DictationHistoryDAO dictationHistoryDAO;

	public DictationService() {}

	public Dictation recommendDictation(long id) {
		Dictation dictation = dictationDAO.get(id);
		if (dictation == null) throw new MissingResourceException("Cannot find dictation", "Dictation", String.valueOf(id));

		dictation.setTotalRecommended(dictation.getTotalRecommended() + 1);
		dictationDAO.persist(dictation);
		return dictation;
	}

	public Dictation addHistory(CreateDictationHistoryRequest request) {
		Dictation dictation = dictationDAO.get(request.dictationId);
		if (dictation == null) throw new MissingResourceException("Cannot find dictation", "Dictation", String.valueOf(request.dictationId));

		createAndSaveDictationHistory(dictation, null, request.mark);
		return updateDictation(request, dictation);
	}

	private Dictation updateDictation(CreateDictationHistoryRequest request, Dictation dictation) {
		dictation.setTotalAttempt(dictation.getTotalAttempt()+1);
		dictation.setLastPracticeDate(new Date());

		Map<Long, Vocab> wordVocabMap = dictation.vocabToMap();
		request.histories.stream().forEach(
				h -> updateVocab(h, wordVocabMap.get(h.question.getId()))
		);
		dictationDAO.persist(dictation);
		return dictation;
	}

	private void updateVocab(VocabPracticeHistory h, Vocab vocab) {
		if (vocab == null) return;

		if (h.correct)
			vocab.setTotalCorrect(vocab.getTotalCorrect() + 1);
		else
			vocab.setTotalWrong(vocab.getTotalWrong() + 1);

		dictationDAO.persist(vocab);
	}

	public DictationHistory createAndSaveDictationHistory(Dictation dictation, Member member, int mark) {
		if (dictation == null) return null;

		DictationHistory history = new DictationHistory();
		history.setDictation(dictation);
		history.setMark(mark);
		if (member != null) {
			history.setPracticer(member);
			history.setPracticerName(member.getName().toString());
			history.setPracticerSchool(member.getSchool());
			history.setPracticerAgeGroup(Dictation.AgeGroup.getAgeGroup(member.getAge()));
		}
		dictationHistoryDAO.persist(history);
		return history;
	}

	public Dictation createAndSaveDictation(Member creator, CreateDictationRequest request) {
		Dictation d = dictationFrom(request);
		d.setCreator(creator);
		dictationDAO.persist(d);
		logger.info("Dictation created: {}", d.toString());
		return d;
	}

	private Dictation dictationFrom(CreateDictationRequest request) {
		Dictation d = new Dictation();
		d.setTitle(request.title);
		d.setDescription(request.description);
		d.setSuitableStudent(request.suitableStudent);

		List<Vocab> vocabs = request.vocabulary.stream().map(word -> {
			Vocab vocab = new Vocab(word);
			vocab.setDictation(d);
			return vocab;
		}).collect(Collectors.toList());
		d.setVocabs(vocabs);

		return d;
	}
}
