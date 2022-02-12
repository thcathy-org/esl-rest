package com.esl.service;

import com.esl.dao.PhoneticQuestionDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.Vocab;
import com.esl.enumeration.VocabDifficulty;
import com.esl.model.PhoneticPractice;
import com.esl.model.PhoneticQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VocabService {
    private static Logger log = LoggerFactory.getLogger(VocabService.class);

    @Value("${PhoneticPractice.MaxQuestions}") int maxQuestions;

    @Autowired PhoneticQuestionService phoneticQuestionService;
    @Autowired PhoneticQuestionDAO phoneticQuestionDAO;

    public PhoneticQuestion createQuestion(String word, boolean showImage, boolean includeBase64Image) {
        log.info("create practice showImage[{}] for vocab: {}", showImage, word);

        return phoneticQuestionService.getQuestionFromDBWithImage(word, showImage, includeBase64Image)
                .orElseGet(() -> phoneticQuestionService.buildQuestionByWebAPI(word, showImage, includeBase64Image));
    }

    public Dictation generatePractice(VocabDifficulty difficulty) {
        log.info("generatePractice {} questions with difficulty {}", PhoneticPractice.MAX_QUESTIONS, difficulty);
        List<PhoneticQuestion> questions = phoneticQuestionDAO.getRandomQuestionWithinRank(difficulty.rank, maxQuestions);

        var vocabs = questions.stream().map(q -> new Vocab(q.getWord())).collect(Collectors.toList());
        return Dictation.vocabPractice(vocabs, difficulty);
    }

}
