package com.esl.entity.dictation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is decorator of dictation that use by practice controller
 */
public class DictationPractice  {
	private static Logger log = LoggerFactory.getLogger(DictationPractice.class);

	public final Dictation dictation;
	public final List<String> sentences;

	public Dictation getDictation() { return dictation; }
	public List<String> getSentences() {return sentences;}

	public DictationPractice(Dictation dictation, List<String> sentences) {
		this.dictation = dictation;
		this.sentences = sentences;
	}


}
