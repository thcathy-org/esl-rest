package com.esl.service;

import com.esl.entity.dictation.SentenceHistory;
import com.esl.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class ArticleDictationService {
    private static Logger log = LoggerFactory.getLogger(ArticleDictationService.class);

    @Value("${Dictation.Article.MaxSentenceLength}") public int maxSentenceLength;

    public SentenceHistory compare(String question, String answer) {
        log.info("compare: question [{}]", question);
        log.info("compare: answer   [{}]", answer);

        List<String> questionSegments = Arrays.asList(question.split(" "));
        List<String> answerSegments = Arrays.asList(answer.split(" "));
        List<Boolean> isCorrect = new ArrayList<>(questionSegments.size());

        int answerPosition = 0;
        for (int questionPosition=0; questionPosition < questionSegments.size(); questionPosition++) {
            boolean correct = false;
            // no more answer
            if (answerPosition >= answerSegments.size()) {
                isCorrect.add(correct);
                continue;
            }

            String questionSegment = questionSegments.get(questionPosition);
            String questionAlphabet = ValidationUtil.alphabetOnly(questionSegment);
            String answerAlphabet = ValidationUtil.alphabetOnly(answerSegments.get(answerPosition));

            if (isBlank(questionAlphabet))
                correct = true;
            else {
                for (int i = answerPosition; i < answerSegments.size(); i++) {
                    String subAnswerAlphabet = ValidationUtil.alphabetOnly(answerSegments.get(i));
                    if (ValidationUtil.wordEqual(questionAlphabet, subAnswerAlphabet)) {
                        correct = true;
                        answerPosition = i-1;
                        break;
                    }
                }
            }

            if (questionAndAnsNotBlank(questionAlphabet, answerAlphabet)
                    || questionAndAnsBlank(questionAlphabet, answerAlphabet))
                if (correct || !answerSizeLeftIsSmaller(questionPosition, questionSegments.size(), answerPosition, answerSegments.size()))
                    answerPosition++;

            isCorrect.add(correct);
        }

        log.info("compare result: segments {}", StringUtils.join(questionSegments));
        log.info("compare result: isCorrect {}", StringUtils.join(isCorrect));

        return new SentenceHistory(question, answer, questionSegments, isCorrect);
    }

    private boolean answerSizeLeftIsSmaller(int questionPosition, int questionSize, int answerPosition, int ansSize) {
        return (ansSize - answerPosition) < (questionSize - questionPosition);
    }

    private boolean questionAndAnsBlank(String questionAlphabet, String answerAlphabet) {
        return isBlank(questionAlphabet) && isBlank(answerAlphabet);
    }

    private boolean questionAndAnsNotBlank(String questionAlphabet, String answerAlphabet) {
        return !isBlank(questionAlphabet) && !isBlank(answerAlphabet);
    }

    public List<String> divideArticleToSentences(String article) {
        if (StringUtils.isBlank(article)) return Collections.EMPTY_LIST;

        return Arrays.stream(article.split("\\r?\\n"))
                .map(s -> s.replaceAll("\t",""))
                .map(String::trim)
                .flatMap(this::splitLongLineByFullstop)
                .flatMap(this::splitLongLingByComma)
                .flatMap(this::splitLongLineBySpace)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private Stream<String> splitLongLingByComma(String input) {
        if (input.length() < maxSentenceLength)
            return Stream.of(input);

        log.info("Split long sentence by comma: {}", input);

        List<String> results = new ArrayList<>();
        while (input.length() > maxSentenceLength - 10) {
            int commaPos = input.indexOf(',', maxSentenceLength-10);
            if (commaPos < 0) {
                results.add(input);
                break;
            }
            results.add(input.substring(0, commaPos));
            input = input.substring(commaPos);
        }

        return results.stream();
    }

    private Stream<String> splitLongLineBySpace(String input) {
        if (input.length() < maxSentenceLength)
            return Stream.of(input);

        log.info("Split long sentence space: {}", input);

        List<String> results = new ArrayList<>();
        while (input.length() > maxSentenceLength) {
            int spacePos = input.indexOf(' ', maxSentenceLength);
            if (spacePos < 0)
                break;

            results.add(input.substring(0, spacePos).trim());
            input = input.substring(spacePos);
        }
        if (input.length() > 0) results.add(input.trim());

        return results.stream();
    }

    private Stream<String> splitLongLineByFullstop(String input) {
        if (input.length() < maxSentenceLength)
            return Stream.of(input);
        else
            return Arrays.stream(input.split("\\. "))
                    .map(s -> s.endsWith(".") ? s : s + ".")
                    .filter(s -> s.matches(".*[a-zA-Z]+.*"))
                    .map(String::trim);
    }
}
