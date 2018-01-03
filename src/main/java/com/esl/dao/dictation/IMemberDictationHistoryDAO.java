package com.esl.dao.dictation;

import com.esl.dao.IESLDao;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.MemberDictationHistory;
import com.esl.model.Member;

import java.util.List;

public interface IMemberDictationHistoryDAO extends IESLDao<MemberDictationHistory> {
	public List<MemberDictationHistory> listByDictation(Dictation dictation);
	public List<MemberDictationHistory> listByDictation(Dictation dictation, int maxResult);

	public List<MemberDictationHistory> listByMember(Member member, int maxResult);
	public MemberDictationHistory loadByDictationMember(Member member, Dictation dictation);
	public int removeByDictation(Dictation dictation);

	/**
	 * Get the lastest dictation history done by input member
	 */
	public MemberDictationHistory getLastestByMember(Member member);
}
