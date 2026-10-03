package com.esl.dao.dictation;

import com.esl.dao.IESLDao;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationSearchCriteria;
import com.esl.model.Member;
import com.esl.model.group.MemberGroup;

import java.util.List;
import java.util.Map;

public interface IDictationDAO extends IESLDao<Dictation> {
	public List<Dictation> listMostPracticed(int maxResult);

	public List<Dictation> listHighestRating(int minRated, int maxResult);

	public List<Dictation> listMostRecommended(int minRecommended, int maxResult);

	public List<Dictation> listLatestPracticed(int maxResult);

	public List<Dictation> listNewCreated(int maxResult);

	public List<Dictation> listByMember(Member member);

	public List<Dictation> listByMemberGroup(MemberGroup group, int maxResult);

	/**
	 * Remove a dictation with dependency
	 */
	public void remove(Dictation dictation);

	/**
	 * Search dictations
	 */
	public List<Dictation> searchDictation(Map<DictationSearchCriteria, Object> searchCriteria, int maxResult);

	/**
	 * Random a accessible, no password dictation
	 */
	public Dictation randomAccessibleDictation(Member member);

	/**
	 * Get total dictation created by member
	 */
	public int getTotalDictationByMember(Member member);

	/**
	 * Get total attempted of all dictation created by the member
	 */
	public int getTotalAttemptedByMember(Member member);

	public Dictation getMostAttemptedByMember(Member member);

	public Dictation getHighestRatingByMember(Member member);

	public Dictation getMostRecommendedByMember(Member member);

	public void refreshDictation(Dictation dictation);
}
