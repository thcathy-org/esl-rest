package com.esl.dao.dictation;

import java.util.List;
import java.util.Optional;

import com.esl.dao.IESLDao;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationHistory;
import com.esl.model.Member;

public interface IDictationHistoryDAO extends IESLDao<DictationHistory> {
	public List<DictationHistory> listByDictation(Dictation dictation);
	public List<DictationHistory> listByDictation(Dictation dictation, int maxResult);
	public List<DictationHistory> listByMember(Member member, int maxResult);
	public List<DictationHistory> listAnnoymousHistoryByDictation(Dictation dictation, int maxResult);

	public int removeByDictation(Dictation dictation);

	/**
     * Get the lastest history of all the dictation created by the member
     */
	public Optional<DictationHistory> getLastestOfAllDictationByMember(Member member);
}
