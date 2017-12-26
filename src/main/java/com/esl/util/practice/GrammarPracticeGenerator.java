package com.esl.util.practice;

import com.esl.entity.practice.GrammarPractice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrammarPracticeGenerator {
	private static Logger log = LoggerFactory.getLogger(GrammarPracticeGenerator.class);

	static public List<String> retrieveQuestions(GrammarPractice practice)  {
		if (practice == null || practice.getPassage() == null) return null;

		Pattern p = Pattern.compile(practice.getQuestionsRegEx(), Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(practice.getPassage());

		List<String> results = new ArrayList<String>();
		int i=0;
		while (matcher.find()) {
			results.add(matcher.group(i));
		}
		return results;
	}

	/**
	 * To random some of the questions from a list of possible question
	 * @return a list of positions of the questions list
	 */
	static public List<Integer> randomQuestionPositions(List<String> questions, int maxQuestions) {
		List<Integer> questionPositions = new ArrayList<Integer>();
		if (questions == null || maxQuestions <= 0) {
			return questionPositions;
		}

		log.debug("randomQuestionPositions: questions size [{}], maxQ [{}]", questions.size(), maxQuestions);

		// prepare positions for random pick up
		for (int i=0; i < questions.size(); i++) questionPositions.add(i);

		// return all positions if input pos size small than max
		if (questions.size() < maxQuestions) return questionPositions;

		Random r = new Random();
		while (questionPositions.size() > maxQuestions) {
			questionPositions.remove(r.nextInt(questionPositions.size()));
		}

		return questionPositions;
	}

	static public String getHTMLPassageWithQuestionNumber(GrammarPractice practice) {
		if (practice == null || practice.getQuestionReplacePattern() == null || practice.getQuestions().size() < 1 || practice.getQuestionPositions().size() < 1) return "";
		log.debug("getHTMLPassageWithQuestionNumber: total possible questions [{}], total questions in practice [{}]", practice.getQuestions().size(),  practice.getQuestionPositions().size());

		Pattern p = Pattern.compile(practice.getQuestionsRegEx(), Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(HtmlUtils.htmlEscape(practice.getPassage()));

		List<Integer> questionPositions = practice.getQuestionPositions();
		StringBuffer sb = new StringBuffer();
		int positionIndex = 0;
		int position = questionPositions.get(positionIndex);

		for (int i=0; i < practice.getQuestions().size(); i++) {
			matcher.find();

			if (i == position) {
				matcher.appendReplacement(sb, practice.getQuestionReplacePattern().replaceAll("\\{\\?\\}", String.valueOf(positionIndex + 1)));

				positionIndex++;
				if (positionIndex < questionPositions.size()) position = questionPositions.get(positionIndex);
			} else {
				matcher.appendReplacement(sb, matcher.group());
			}
		}
		matcher.appendTail(sb);
		return sb.toString().replaceAll("(\r\n|\n)", "<br/>");
	}

	static public String getHTMLPassageWithAnswer(GrammarPractice practice) {
		if (practice == null || practice.getQuestionMatchingPattern() == null || practice.getAnswerReplacePattern() == null || practice.getHtmlPassageWithQuestions() == null) return "";

		Pattern p = Pattern.compile(practice.getQuestionMatchingPattern());
		Matcher matcher = p.matcher(practice.getHtmlPassageWithQuestions());
		int index = 0;
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String replacedString = practice.getAnswerReplacePattern().replaceAll("\\{answer\\}", practice.getQuestions().get(practice.getQuestionPositions().get(index)));
			replacedString = replacedString.replaceAll("\\{number\\}", String.valueOf(index+1));
			matcher.appendReplacement(sb, replacedString);

			index++;
			if (index > practice.getQuestionPositions().size()) break;		// potection if input passage contain questionPattern
		}

		matcher.appendTail(sb);
		return sb.toString().replaceAll("(\r\n|\n)", "<br/>");
	}

}
