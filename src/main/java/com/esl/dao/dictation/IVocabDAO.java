package com.esl.dao.dictation;

import java.util.*;

import com.esl.dao.IESLDao;
import com.esl.entity.dictation.*;

public interface IVocabDAO extends IESLDao<Vocab> {
	public void removeByDictation(Dictation dictation);
}
