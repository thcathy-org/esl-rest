package com.esl.service.memberword;

import com.esl.dao.MemberWordDAO;
import com.esl.exception.IllegalParameterException;
import com.esl.model.Member;
import com.esl.model.MemberWord;
import com.esl.model.PhoneticQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service("memberWordManageService")
public class MemberWordManageService implements IMemberWordManageService {
	private static Logger logger = LoggerFactory.getLogger(MemberWordManageService.class);
	private static int MAX_WORD = 100;

	//	 supporting class
	@Autowired
    private MemberWordDAO memberWordDAO;

	//	 ============== Setter / Getter ================//
	public void setMemberWordDAO(MemberWordDAO memberWordDAO) {this.memberWordDAO = memberWordDAO;	}
	public void setMaxWord(int max) { this.MAX_WORD = max; }

	//	 ============== Constructor ================//
	public MemberWordManageService() {}

	//	 ============== Functions ================//

	public int deleteWords(List<MemberWord> words) {
		if (words == null) throw new IllegalParameterException(new String[]{"member words list"}, new Object[]{words});
		logger.info("deleteWords: list size[" + words.size() + "]");

		for (MemberWord word :words) {
			logger.info("deleteWords: delete word[" + word.getMember().getUserId() + "," + word.getWord().getWord() +"]");
			memberWordDAO.transit(word);
		}
		return words.size();
	}

	public int deleteLearntWords(Member member) {
		if (member == null) throw new IllegalParameterException(new String[]{"member"}, new Object[]{member});
		logger.info("deleteLearntWords: Delete by [" + member.getUserId() + "]");
		int counter = memberWordDAO.deleteWordsByCorrectCount(member, MemberWordDAO.LEARNT_CORRECT_REQUIRE);
		logger.info("deleteLearntWords: Deleted total [" + counter + "] words");
		return counter;
	}

	public String saveWord(Member member, PhoneticQuestion word) {
		if (member == null || word == null) throw new IllegalParameterException(new String[]{"member","word"}, new Object[]{member,word});
		logger.info("saveWord: member[" + member.getUserId() + "], word[" + word.getWord() + "]");

		// Check over max word
		int totalWords = memberWordDAO.totalWords(member);
		logger.info("saveWord: member owned words [" + totalWords + "]");
		if (totalWords >= MAX_WORD) return OVER_MAX_WORDS;

		MemberWord memberWord = null;

		// Check the word is saved before
		memberWord = memberWordDAO.getWord(member, word);
		if (memberWord != null) {
			logger.info("saveWord: already saved");
			return WORD_ALREADY_SAVED;
		}

		// Create new member-word
		memberWord = new MemberWord(member, word);
		memberWordDAO.persist(memberWord);
		logger.info("saveWord: member-word saved");

		return WORD_SAVED;
	}


}
