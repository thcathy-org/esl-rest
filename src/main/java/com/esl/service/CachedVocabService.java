package com.esl.service;

import com.esl.model.PhoneticQuestion;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class CachedVocabService {
    private static final Logger log = LoggerFactory.getLogger(CachedVocabService.class);

    @Value("${CachedVocabService.CacheSize}") int cacheSize;

    @Autowired VocabService vocabService;

    Cache<String, PhoneticQuestion> cache;

    @PostConstruct
    public void init() {
        cache = Caffeine.newBuilder()
                .maximumSize(cacheSize)
                .build();
    }

    public PhoneticQuestion createQuestion(String word, boolean showImage) {
        return cache.get(key(word, showImage), (k) -> vocabService.createQuestion(word, showImage));
    }

    String key(String word, boolean showImage) {
        return String.format("%s:%s", word, showImage);
    }
}
