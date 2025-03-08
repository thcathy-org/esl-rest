package com.esl.service.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class ReplicateAIService {
    final WebClient webClient;
    final Duration requestTimeout;

    public ReplicateAIService(
            @Value("${REPLICATE_API_TOKEN:test_value}") String apiToken,
            @Value("${ReplicateAIService.RequestTimeoutInSecond}") int requestTimeoutInSecond
    ) {
        this.requestTimeout = Duration.ofSeconds(requestTimeoutInSecond);
        this.webClient = WebClient.builder()
                .baseUrl("https://api.replicate.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
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
                .flatMap(errorBody -> {
                    String errorMessage = String.format("Failed to get definition: %s\nResponse body: %s", 
                            clientResponse.statusCode(), errorBody);
                    System.err.println(errorMessage);
                    return Mono.error(new RuntimeException(errorMessage));
                });
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
