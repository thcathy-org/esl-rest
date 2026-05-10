package com.esl.service.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ReplicateAIService {
    private static final Logger logger = LoggerFactory.getLogger(ReplicateAIService.class);

    final WebClient webClient;
    final Duration requestTimeout;
    private final boolean configured;

    @Value("${ReplicateAIService.InworldTts.VoiceId:Ashley}")
    private String inworldVoiceId;

    @Value("${ReplicateAIService.InterpretationModel:}")
    private String interpretationModel;

    public ReplicateAIService(
            @Value("${REPLICATE_API_TOKEN:}") String apiToken,
            @Value("${ReplicateAIService.RequestTimeoutInSecond}") int requestTimeoutInSecond
    ) {
        var token = StringUtils.trimToNull(apiToken);
        this.requestTimeout = Duration.ofSeconds(requestTimeoutInSecond);

        if (token == null) {
            logger.warn("ReplicateAIService not configured: REPLICATE_API_TOKEN is missing");
            this.configured = false;
            this.webClient = null;
            return;
        }

        this.configured = true;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.replicate.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public boolean isConfigured() {
        return configured;
    }
    
    public byte[] inworldTextToSpeech(String text) {
        if (!configured) {
            throw new IllegalStateException("ReplicateAIService is not configured");
        }

        var input = Map.of(
                "text", text,
                "voice_id", inworldVoiceId,
                "audio_format", "mp3",
                "sample_rate", 24000,
                "speaking_rate", 1.0
        );

        logger.info("Calling Inworld TTS via Replicate for text={}", text);

        var prediction = webClient.post()
                .uri("/models/inworld/realtime-tts-1.5-mini/predictions")
                .header("Prefer", "wait=60")
                .bodyValue(Map.of("input", input))
                .retrieve()
                .onStatus(status -> status.value() >= 400, this::handleErrorResponse)
                .bodyToMono(TtsPredictionResponse.class)
                .timeout(requestTimeout)
                .block();

        if (prediction == null) {
            throw new IllegalStateException("Inworld TTS returned null prediction");
        }
        if (!"succeeded".equals(prediction.status)) {
            prediction = pollPrediction(prediction.id);
        }

        var outputUrl = prediction.getOutputUrl();
        if (outputUrl == null) {
            throw new IllegalStateException("Inworld TTS prediction has no output URL");
        }
        return downloadAudio(outputUrl);
    }

    private TtsPredictionResponse pollPrediction(String id) {
        var deadline = System.currentTimeMillis() + requestTimeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            var poll = webClient.get()
                    .uri("/predictions/" + id)
                    .retrieve()
                    .onStatus(status -> status.value() >= 400, this::handleErrorResponse)
                    .bodyToMono(TtsPredictionResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (poll == null) {
                throw new IllegalStateException("Inworld TTS poll returned null for prediction " + id);
            }
            if ("succeeded".equals(poll.status)) return poll;
            if ("failed".equals(poll.status) || "canceled".equals(poll.status)) {
                throw new RuntimeException("Inworld TTS prediction " + id + " " + poll.status + ": " + poll.error);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while polling Inworld TTS prediction " + id, e);
            }
        }
        throw new RuntimeException("Inworld TTS prediction " + id + " timed out");
    }

    private byte[] downloadAudio(String url) {
        return WebClient.create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    public String getInterpretation(String text, String lang) {
        if (!configured) {
            throw new IllegalStateException("ReplicateAIService is not configured");
        }
        if (StringUtils.isBlank(interpretationModel)) {
            throw new IllegalStateException("ReplicateAIService.InterpretationModel is not configured");
        }

        var prompt = buildInterpretationPrompt(text, lang);

        var input = Map.of(
                "prompt", prompt,
                "max_tokens", 256,
                "reasoning_effort", "minimal"
        );

        logger.info("Calling Replicate {} for interpretation lang={} textLen={}", interpretationModel, lang, text.length());

        var response = webClient.post()
                .uri("/models/" + interpretationModel + "/predictions")
                .header("Prefer", "wait")
                .bodyValue(Map.of("input", input))
                .retrieve()
                .onStatus(status -> status.value() >= 400, this::handleErrorResponse)
                .bodyToMono(PredictionResponse.class)
                .timeout(requestTimeout)
                .block();

        if (response == null || response.output == null) {
            throw new RuntimeException("Empty response from interpretation API");
        }
        return String.join("", response.output)
                .replaceAll("[\\r\\n]+", " ")
                .trim();
    }

    private String buildInterpretationPrompt(String text, String lang) {
        if ("en".equals(lang)) {
            return "You are an English dictionary for ESL learners. Define the text within triple quotes in fewer than 30 words, on one line. " +
                    "Strict rule: do NOT include the input text or any morphological form of its words (e.g. plural, gerund, past tense) anywhere in your definition - use synonyms or paraphrases instead. " +
                    "'''" + text + "'''";
        }
        var chinese = "zh-Hant".equals(lang) ? "Traditional Chinese" : "Simplified Chinese";
        return "You are a professional English-to-Chinese translator for an ESL learning app. " +
                "Translate the text within triple quotes to " + chinese + ". " +
                "Output only the " + chinese + " translation. No quotes, no pinyin, no explanation, no English. " +
                "'''" + text + "'''";
    }

    public List<String> getDefinition(String term) {
        String prompt = "Provide a definition of the text within triple quote. Output less than 30 words but you can return as less as possible. Do not use any word within triple quote in the definition. Output the definition in one line. '''" + term + "'''";

        PredictionInput input = new PredictionInput();
        input.prompt = prompt;
        input.maxTokens = 128;
        input.systemPrompt = "You are an English dictionary";
        input.maxNewTokens = 128;

        PredictionRequest request = new PredictionRequest();
        request.setInput(input);

        PredictionResponse response = webClient.post()
                .uri("/models/meta/meta-llama-3-8b-instruct/predictions")
                .header("Prefer", "wait")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, this::handleErrorResponse)
                .bodyToMono(PredictionResponse.class)
                .timeout(requestTimeout)
                .block();
        
        if (response != null && response.output != null) {
            return response.output;
        } else {
            throw new RuntimeException("Empty response from API");
        }
    }
    
    private Mono<? extends Throwable> handleErrorResponse(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> Mono.error(new RuntimeException(
                        "Replicate API error: " + clientResponse.statusCode() + "\nResponse body: " + errorBody)));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TtsPredictionResponse {
        public String id;
        public String status;
        public Object output;
        public String error;

        public String getOutputUrl() {
            if (output instanceof String s) return s;
            if (output instanceof List<?> list && !list.isEmpty()) return String.valueOf(list.get(0));
            return null;
        }
    }

    static class PredictionInput {
        @JsonProperty("prompt")
        public String prompt;
        @JsonProperty("max_tokens")
        public int maxTokens;
        @JsonProperty("system_prompt")
        public String systemPrompt;
        @JsonProperty("max_new_tokens")
        public int maxNewTokens;
    }

    static class PredictionRequest {
        private PredictionInput input;

        public PredictionInput getInput() { return input; }
        public void setInput(PredictionInput input) { this.input = input; }
    }

    static class PredictionResponse {
        public List<String> output;
    }
}
