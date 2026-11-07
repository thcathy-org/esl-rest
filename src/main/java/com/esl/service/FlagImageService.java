package com.esl.service;

import com.esl.dao.repository.FlagImageRepository;
import com.esl.entity.FlagImage;
import com.esl.service.rest.ImageGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class FlagImageService {
    private static final Logger logger = LoggerFactory.getLogger(FlagImageService.class);
    private static final int MAX_PHRASE_LENGTH = 500;

    private final FlagImageRepository repository;
    private final ImageGenerationService imageGenerationService;
    private final String imagesHost;

    public FlagImageService(
            FlagImageRepository repository,
            ImageGenerationService imageGenerationService,
            @Value("${FlagImage.ImagesHost:https://firebasestorage.googleapis.com/v0/b/friendlyeats-7fa2f.appspot.com/o/images}") String imagesHost) {
        this.repository = repository;
        this.imageGenerationService = imageGenerationService;
        this.imagesHost = imagesHost;
    }

    @Transactional
    public FlagImage flag(String phrase, int imageIndex) {
        var cleanedPhrase = requirePhrase(phrase);
        requireIndex(imageIndex);
        repository.addFlag(cleanedPhrase, imageIndex, new Date());
        var flagImage = repository.findByPhraseAndImageIndex(cleanedPhrase, imageIndex).orElseThrow();
        logger.info("Flagged image id={} phrase='{}' index={} count={}", flagImage.getId(), cleanedPhrase, imageIndex, flagImage.getFlagCount());
        return flagImage;
    }

    public List<FlaggedImage> list() {
        var flaggedImages = new ArrayList<FlaggedImage>();
        repository.findAll().forEach(flagImage -> flaggedImages.add(new FlaggedImage(flagImage.getPhrase(), flagImage.getImageIndex(), flagImage.getFlagCount())));
        flaggedImages.sort(Comparator.comparingInt(FlaggedImage::count).reversed()
                .thenComparing(FlaggedImage::phrase)
                .thenComparingInt(FlaggedImage::imageIndex));
        return flaggedImages;
    }

    public void remove(String phrase, int imageIndex) {
        var cleanedPhrase = requirePhrase(phrase);
        requireIndex(imageIndex);
        logger.info("Removing image phrase='{}' index={}", cleanedPhrase, imageIndex);
        imageGenerationService.removeImage(cleanedPhrase, imageIndex);
        repository.deleteByPhraseAndImageIndex(cleanedPhrase, imageIndex);
    }

    public String imagesHost() {
        return imagesHost;
    }

    static String requirePhrase(String phrase) {
        if (phrase == null || phrase.isBlank()) {
            throw new IllegalArgumentException("phrase is required");
        }
        var cleanedPhrase = phrase.trim();
        if (cleanedPhrase.length() > MAX_PHRASE_LENGTH) {
            throw new IllegalArgumentException("phrase is too long");
        }
        return cleanedPhrase;
    }

    private static void requireIndex(int imageIndex) {
        if (imageIndex < 0) {
            throw new IllegalArgumentException("imageIndex must be >= 0");
        }
    }

    public record FlaggedImage(String phrase, int imageIndex, int count) {}
}
