package com.esl.service;

import com.esl.dao.repository.FlagImageRepository;
import com.esl.entity.FlagImage;
import com.esl.service.rest.ImageGenerationService;
import com.esl.service.rest.ImageJsonUrl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlagImageService {
    private static final Logger logger = LoggerFactory.getLogger(FlagImageService.class);
    private static final int MAX_PHRASE_LENGTH = 500;

    private final FlagImageRepository repository;
    private final ImageGenerationService imageGenerationService;
    private final ObjectMapper objectMapper;
    private final WebClient publicWebClient;
    private final String imagesHost;
    private final Duration timeout;

    public FlagImageService(
            FlagImageRepository repository,
            ImageGenerationService imageGenerationService,
            ObjectMapper objectMapper,
            @Value("${FlagImage.ImagesHost:https://firebasestorage.googleapis.com/v0/b/friendlyeats-7fa2f.appspot.com/o/images}") String imagesHost,
            @Value("${ImageGenerationService.RequestTimeoutInSecond}") int timeoutSeconds) {
        this.repository = repository;
        this.imageGenerationService = imageGenerationService;
        this.objectMapper = objectMapper;
        this.imagesHost = imagesHost;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.publicWebClient = WebClient.builder().build();
    }

    public FlagImage flag(String phrase, int imageIndex, Long memberId) {
        var cleaned = requirePhrase(phrase);
        requireIndex(imageIndex);
        var row = new FlagImage();
        row.setPhrase(cleaned);
        row.setImageIndex(imageIndex);
        row.setMemberId(memberId);
        row.setCreatedDate(new Date());
        row.setDone(false);
        var saved = repository.save(row);
        logger.info("Stored flagged image id={} phrase='{}' index={} member={}", saved.getId(), cleaned, imageIndex, memberId);
        return saved;
    }

    public List<FlagImageGroup> openGroups() {
        var counts = new LinkedHashMap<GroupKey, Integer>();
        for (var flaggedImage : repository.findByDoneFalse()) {
            counts.merge(new GroupKey(flaggedImage.getPhrase(), flaggedImage.getImageIndex()), 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .map(entry -> new FlagImageGroup(entry.getKey().phrase(), entry.getKey().imageIndex(), entry.getValue()))
                .sorted(Comparator.comparingInt(FlagImageGroup::count).reversed()
                        .thenComparing(FlagImageGroup::phrase)
                        .thenComparingInt(FlagImageGroup::imageIndex))
                .toList();
    }

    public void remove(String phrase, int imageIndex) {
        var cleaned = requirePhrase(phrase);
        requireIndex(imageIndex);
        var imageCount = countImages(cleaned);
        if (imageCount == 1) {
            logger.info("Removing last image for phrase='{}' index={}", cleaned, imageIndex);
            imageGenerationService.postJson("/verify/remove", json(Map.of("phrase", cleaned)));
        } else {
            logger.info("Removing image index {} for phrase='{}' (imageCount={})", imageIndex, cleaned, imageCount);
            imageGenerationService.postJson("/verify/verified", json(List.of(Map.of(
                    "phrase", cleaned,
                    "removeImageIndex", List.of(imageIndex)
            ))));
        }
        repository.markDone(cleaned, imageIndex);
    }

    public String imagesHost() {
        return imagesHost;
    }

    int countImages(String phrase) {
        var url = ImageJsonUrl.publicUrl(imagesHost, phrase);
        try {
            var body = publicWebClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(timeout);
            if (body == null) return -1;
            var images = objectMapper.readTree(body).get("images");
            if (images == null || !images.isArray()) return -1;
            return images.size();
        } catch (Exception e) {
            logger.warn("Image count unavailable for '{}': {}", phrase, e.toString());
            return -1;
        }
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

    private record GroupKey(String phrase, int imageIndex) {}

    public record FlagImageGroup(String phrase, int imageIndex, int count) {}
}
