package com.esl.service;

import com.esl.dao.PhoneticQuestionDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.Vocab;
import com.esl.enumeration.VocabDifficulty;
import com.esl.model.PhoneticPractice;
import com.esl.model.PhoneticQuestion;
import com.esl.service.rest.ReplicateAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VocabService {
    private static Logger log = LoggerFactory.getLogger(VocabService.class);

    @Value("${PhoneticPractice.MaxQuestions}") int maxQuestions;

    final PhoneticQuestionService phoneticQuestionService;
    final PhoneticQuestionDAO phoneticQuestionDAO;
    final ReplicateAIService replicateAIService;

    public VocabService(PhoneticQuestionService phoneticQuestionService, PhoneticQuestionDAO phoneticQuestionDAO, ReplicateAIService replicateAIService) {
        this.phoneticQuestionService = phoneticQuestionService;
        this.phoneticQuestionDAO = phoneticQuestionDAO;
        this.replicateAIService = replicateAIService;
    }

    public PhoneticQuestion createQuestion(String word, boolean showImage) {
        log.info("create practice showImage[{}] for vocab: {}", showImage, word);

        return phoneticQuestionService.getQuestionFromDBWithImage(word)
                .orElseGet(() -> phoneticQuestionService.buildQuestionByWebAPI(word, showImage));
    }

    public Dictation generatePractice(VocabDifficulty difficulty) {
        log.info("generatePractice {} questions with difficulty {}", PhoneticPractice.MAX_QUESTIONS, difficulty);
        List<PhoneticQuestion> questions = phoneticQuestionDAO.getRandomQuestionWithinRank(difficulty.rank, maxQuestions);

        var vocabs = questions.stream().map(q -> new Vocab(q.getWord())).collect(Collectors.toList());
        return Dictation.vocabPractice(vocabs, difficulty);
    }

    public String getMeaning(String word) {
        log.info("get meaning for '{}' using AI", word);
        var aiResponse = replicateAIService.getDefinition(word);
        String definition = String.join("", aiResponse);

        String[] wordParts = word.toLowerCase().replaceAll("[^a-zA-Z\\s-]", "").split("[-\\s]+");

        String maskedDefinition = definition;
        for (String part : wordParts) {
            if (part.length() > 2) { // Only mask words with more than 2 characters
                String pattern = "(?i)" + part;
                maskedDefinition = maskedDefinition.replaceAll(pattern, "***");
            }
        }
        
        log.debug("Original definition: '{}', masked: '{}'", definition, maskedDefinition);
        return maskedDefinition;
    }
}
