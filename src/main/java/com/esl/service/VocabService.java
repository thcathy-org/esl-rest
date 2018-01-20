package com.esl.service;

import com.esl.model.PhoneticQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VocabService {
    private static Logger log = LoggerFactory.getLogger(VocabService.class);

    @Autowired PhoneticQuestionService phoneticQuestionService;

    public PhoneticQuestion createQuestion(String word, boolean showImage) {
        log.info("create practice showImage[{}] for vocab: {}", showImage, word);

        return phoneticQuestionService.getQuestionFromDBWithImage(word, showImage)
                .orElseGet(() -> phoneticQuestionService.buildQuestionByWebAPI(word, showImage));
    }
}
