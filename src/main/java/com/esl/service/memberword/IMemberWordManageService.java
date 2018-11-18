package com.esl.service.memberword;

import com.esl.model.Member;
import com.esl.model.MemberWord;
import com.esl.model.PhoneticQuestion;

import java.util.List;

public interface IMemberWordManageService {		
				
	public String saveWord(Member member, PhoneticQuestion word);
	public static final String WORD_SAVED = "WORD_SAVED";
	public static final String WORD_ALREADY_SAVED = "WORD_ALREADY_SAVED";
	public static final String OVER_MAX_WORDS = "OVER_MAX_WORDS";
		
	public int deleteWords(List<MemberWord> words);
	public int deleteLearntWords(Member member);
}
