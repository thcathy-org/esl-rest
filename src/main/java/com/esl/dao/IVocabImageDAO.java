package com.esl.dao;

import com.esl.entity.VocabImage;

import java.util.List;

public interface IVocabImageDAO extends IESLDao<VocabImage> {
	public List<VocabImage> listByWord(String word);
	public List<VocabImage> listLatest(int maxRow, int maxid);
}
