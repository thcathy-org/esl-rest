package com.esl.service;

import com.esl.dao.dictation.DictationHistoryDAO;
import com.esl.dao.dictation.IDictationDAO;
import com.esl.dao.dictation.VocabDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationHistory;
import com.esl.entity.dictation.Vocab;
import com.esl.entity.practice.PracticeHistory;
import com.esl.entity.rest.CreateDictationHistoryRequest;
import com.esl.entity.rest.EditDictationRequest;
import com.esl.entity.rest.VocabPracticeHistory;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Transactional
@Service
public class DictationService {
	private static Logger log = LoggerFactory.getLogger(DictationService.class);

	@Autowired private IDictationDAO dictationDAO;
	@Autowired private VocabDAO vocabDAO;
	@Autowired private DictationHistoryDAO dictationHistoryDAO;
	@Autowired private MemberScoreService memberScoreService;
	@Autowired private PracticeHistoryService practiceHistoryService;

	public DictationService() {}

	public Dictation recommendDictation(long id) {
		var dictation = dictationDAO.get(id);
		if (dictation == null) throw new MissingResourceException("Cannot find dictation", "Dictation", String.valueOf(id));

		dictation.setTotalRecommended(dictation.getTotalRecommended() + 1);
		dictationDAO.persist(dictation);
		return dictation;
	}

	public Dictation addHistory(Optional<Member> member, CreateDictationHistoryRequest request) {
		var dictation = dictationDAO.get(request.dictationId);
		if (dictation == null) throw new MissingResourceException("Cannot find dictation", "Dictation", String.valueOf(request.dictationId));
		member.ifPresent(m -> {
			memberScoreService.addScoreToMember(m, request.mark);
			createPracticeHistory(m, request);
		});

		createAndSaveDictationHistory(dictation, null, request.mark);
		return updateDictation(request, dictation);
	}

	private PracticeHistory createPracticeHistory(Member member, CreateDictationHistoryRequest request) {
		var history = new PracticeHistory()
				.setFullMark(request.fullMark)
				.setMark(request.mark)
				.setPercentage(request.percentage)
				.setMember(member)
				.setDictationId(request.dictationId)
				.setHistoryJSON(request.historyJSON);
		return practiceHistoryService.saveNewHistory(history);
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

		vocabDAO.persist(vocab);
	}

	public DictationHistory createAndSaveDictationHistory(Dictation dictation, Member member, int mark) {
		if (dictation == null) return null;

		var history = new DictationHistory();
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

	public Dictation createOrAmendDictation(Member member, EditDictationRequest request) {
		Dictation d = findOrCreateDictation(member, request);
		applyRequestToDictation(request, d, member);
		dictationDAO.persist(d);
		log.info("Dictation created: {}", d.toString());
		return d;
	}

	private Dictation findOrCreateDictation(Member member, EditDictationRequest request) {
		if (request.isCreate()) {
			return new Dictation();
		} else {
			Dictation d = dictationDAO.get(request.dictationId);
			if (d.getCreator().getId() != member.getId()) throw new UnsupportedOperationException(member.getUserId() + " is not creator of dictation: " + d.getId());
			return d;
		}
	}

	private Dictation applyRequestToDictation(EditDictationRequest request, Dictation dictation, Member member) {
		dictation.setTitle(request.title);
		dictation.setDescription(request.description);
		dictation.setSuitableStudent(request.suitableStudent);
		dictation.setCreator(member);
		dictation.setLastModifyDate(new Date());

		if (!CollectionUtils.isEmpty(request.vocabulary)) {
			List<Vocab> newVocabList = request.vocabulary.stream().map(findExistVocabOrCreateNew(dictation)).collect(toList());
			replaceVocabs(dictation, newVocabList);
		} else {
			dictation.setArticle(request.article);
		}

		return dictation;
	}

	private void replaceVocabs(Dictation dictation, List<Vocab> newVocabList) {
		var oldVocabs = dictation.getVocabs();
		oldVocabs.removeAll(newVocabList);
		vocabDAO.deleteAll(oldVocabs);
		dictation.setVocabs(newVocabList);
	}

	private Function<String, Vocab> findExistVocabOrCreateNew(Dictation dictation) {
		return word -> {
			var optVocab = dictation.getVocabs().stream().filter(v -> word.equals(v.getWord())).findFirst();
			Vocab vocab = optVocab.orElseGet(() -> new Vocab(word));
			vocab.setDictation(dictation);
			return vocab;
		};
	}

	public Dictation deleteDictation(String email, long id) {
		var d = dictationDAO.get(id);
		if (d == null || !Objects.equals(d.getCreator().getEmailAddress(), email))
			throw new UnsupportedOperationException("cannot delete dictation");

		dictationDAO.remove(d);
		return d;
	}
}