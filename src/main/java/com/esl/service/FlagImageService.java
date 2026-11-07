package com.esl.service;

import com.esl.dao.repository.FlagImageRepository;
import com.esl.entity.FlagImage;
import com.esl.service.rest.ImageGenerationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class FlagImageService {
    private static final Logger logger = LoggerFactory.getLogger(FlagImageService.class);
    private static final int MAX_PHRASE_LENGTH = 500;

    private final FlagImageRepository repository;
    private final ImageGenerationService imageGenerationService;
    private final ObjectMapper objectMapper;
    private final String imagesHost;

    public FlagImageService(
            FlagImageRepository repository,
            ImageGenerationService imageGenerationService,
            ObjectMapper objectMapper,
            @Value("${FlagImage.ImagesHost:https://firebasestorage.googleapis.com/v0/b/friendlyeats-7fa2f.appspot.com/o/images}") String imagesHost) {
        this.repository = repository;
        this.imageGenerationService = imageGenerationService;
        this.objectMapper = objectMapper;
        this.imagesHost = imagesHost;
    }

    @Transactional
    public FlagImage flag(String phrase, int imageIndex) {
        var cleaned = requirePhrase(phrase);
        requireIndex(imageIndex);
        repository.addFlag(cleaned, imageIndex, new Date());
        var saved = repository.findByPhraseAndImageIndex(cleaned, imageIndex).orElseThrow();
        logger.info("Flagged image id={} phrase='{}' index={} count={}", saved.getId(), cleaned, imageIndex, saved.getFlagCount());
        return saved;
    }

    public List<FlagImageGroup> openGroups() {
        var groups = new ArrayList<FlagImageGroup>();
        repository.findAll().forEach(row -> groups.add(new FlagImageGroup(row.getPhrase(), row.getImageIndex(), row.getFlagCount())));
        groups.sort(Comparator.comparingInt(FlagImageGroup::count).reversed()
                .thenComparing(FlagImageGroup::phrase)
                .thenComparingInt(FlagImageGroup::imageIndex));
        return groups;
    }

    public void remove(String phrase, int imageIndex) {
        var cleaned = requirePhrase(phrase);
        requireIndex(imageIndex);
        logger.info("Removing image phrase='{}' index={}", cleaned, imageIndex);
        imageGenerationService.postJson("/image/remove", json(Map.of(
                "phrase", cleaned,
                "imageIndex", imageIndex
        )));
        repository.deleteByPhraseAndImageIndex(cleaned, imageIndex);
    }

    public String imagesHost() {
        return imagesHost;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not encode image-generation request", e);
        }
    }

    static String requirePhrase(String phrase) {
        if (phrase == null || phrase.isBlank()) {
            throw new IllegalArgumentException("phrase is required");
        }
        var cleaned = phrase.trim();
        if (cleaned.length() > MAX_PHRASE_LENGTH) {
            throw new IllegalArgumentException("phrase is too long");
        }
        return cleaned;
    }

    private static void requireIndex(int imageIndex) {
        if (imageIndex < 0) {
            throw new IllegalArgumentException("imageIndex must be >= 0");
        }
    }

    public record FlagImageGroup(String phrase, int imageIndex, int count) {}
}
