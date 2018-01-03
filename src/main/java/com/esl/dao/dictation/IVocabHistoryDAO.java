package com.esl.dao.dictation;

import java.util.*;

import com.esl.dao.IESLDao;
import com.esl.entity.dictation.*;

public interface IVocabHistoryDAO extends IESLDao<VocabHistory> {
	public int removeByVocabs(Collection<Vocab> vocabs);
}
