package com.esl.service;

import com.esl.dao.repository.VocabInterpretationRepository;
import com.esl.entity.VocabInterpretation;
import com.esl.service.rest.ReplicateAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Set;

@Service
public class InterpretationService {
    private static final Logger log = LoggerFactory.getLogger(InterpretationService.class);
    private static final Set<String> SUPPORTED_LANGS = Set.of("en", "zh-Hans", "zh-Hant");
    static final int MAX_TEXT_LENGTH = 500;

    private final VocabInterpretationRepository repository;
    private final ReplicateAIService replicateAIService;

    public InterpretationService(VocabInterpretationRepository repository, ReplicateAIService replicateAIService) {
        this.repository = repository;
        this.replicateAIService = replicateAIService;
    }

    public String interpret(String rawText, String lang) {
        if (!SUPPORTED_LANGS.contains(lang)) {
            throw new IllegalArgumentException("Unsupported lang: " + lang);
        }

        var text = normalize(rawText);
        if (text.isEmpty() || text.length() > MAX_TEXT_LENGTH) return "";

        var cached = repository.findByTextAndLang(text, lang);
        if (cached.isPresent()) {
            log.debug("DB hit for lang={} text='{}'", lang, text);
            return cached.get().getResult();
        }

        log.info("DB miss; calling Replicate for lang={} text='{}'", lang, text);
        var result = replicateAIService.getInterpretation(text, lang);

        try {
            repository.save(new VocabInterpretation(text, lang, result));
        } catch (DataIntegrityViolationException e) {
            log.info("Concurrent insert race on lang={} text='{}'; returning this thread's result", lang, text);
        }
        return result;
    }

    static String normalize(String input) {
        if (input == null) return "";
        var collapsed = input.trim().toLowerCase().replaceAll("\\s+", " ");
        return Normalizer.normalize(collapsed, Normalizer.Form.NFC);
    }
}
