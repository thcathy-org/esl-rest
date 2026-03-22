package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.service.rest.CloudflareAIService;
import com.esl.service.rest.R2StorageService;
import com.esl.service.rest.SpeechWorkerService;
import com.esl.util.TtsTextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TtsPublisherServiceTest {

    @Mock TtsPublishQueueRepository repository;
    @Mock R2StorageService r2StorageService;
    @Mock SpeechWorkerService speechWorkerService;
    @Mock CloudflareAIService cloudflareAIService;

    TtsPublisherService service;

    @BeforeEach
    @SuppressWarnings({"unchecked", "null"})
    void setUp() {
        var transactionTemplate = mock(TransactionTemplate.class);
        doAnswer(invocation -> {
            var callback = (Consumer<TransactionStatus>) invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        service = new TtsPublisherService(
                transactionTemplate,
                repository,
                r2StorageService,
                speechWorkerService,
                cloudflareAIService
        );
        ReflectionTestUtils.setField(service, "ttsProvider", TtsPublisherService.PROVIDER_SPEECH_WORKER);
        ReflectionTestUtils.setField(service, "defaultTtsVersion", "v1");
        ReflectionTestUtils.setField(service, "backoffSeconds", 60);
        ReflectionTestUtils.setField(service, "maxAttempts", 288);
        ReflectionTestUtils.setField(service, "ttsVoice", "");
        ReflectionTestUtils.setField(service, "batchSize", 100);
    }

    @Test
    void publishNext_shouldSkipWhenR2NotConfigured() {
        when(r2StorageService.isConfigured()).thenReturn(false);

        service.publishNext();

        verify(repository, never()).findNext(anyList(), any(Date.class), anyInt(), any());
    }

    @Test
    void publishNext_shouldDeleteWhenArtifactsExist() {
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(true);

        service.publishNext();

        verify(repository).deleteById(1L);
        verify(speechWorkerService, never()).generate(any());
    }

    @Test
    void publishNext_shouldPublishAndDeleteWhenMissingArtifacts() {
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);

        var response = new SpeechWorkerService.GenerateResponse();
        response.audioBase64 = Base64.getEncoder().encodeToString("hello".getBytes());
        response.audioFormat = "mp3";
        response.mimeType = "audio/mpeg";
        response.sampleRate = 24000;
        response.wordTimestamps = List.of(Map.of("word", "hello", "start", 0.0, "end", 0.5));
        response.originalText = "hello";
        response.processedText = "hello";

        when(speechWorkerService.generate(any(SpeechWorkerService.GenerateRequest.class))).thenReturn(response);

        service.publishNext();

        verify(r2StorageService, times(2)).putBytes(anyString(), any(byte[].class), anyString());
        verify(repository).deleteById(1L);
    }

    @Test
    void publishNext_shouldBuildDeterministicAudioKeysFromNormalizedText() {
        var item = createItem();
        item.setContent("Hello world.");

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);

        var response = new SpeechWorkerService.GenerateResponse();
        response.audioBase64 = Base64.getEncoder().encodeToString("hello".getBytes());
        response.audioFormat = "mp3";
        response.mimeType = "audio/mpeg";
        response.sampleRate = 24000;
        response.originalText = "hello";
        response.processedText = "hello";

        when(speechWorkerService.generate(any(SpeechWorkerService.GenerateRequest.class))).thenReturn(response);

        service.publishNext();

        var normalText = TtsTextUtil.normalize("Hello world.");
        var punctText = TtsTextUtil.normalize(TtsTextUtil.toPunctuationText(normalText));
        var normalHash = TtsTextUtil.sha256Hex(normalText);
        var punctHash = TtsTextUtil.sha256Hex(punctText);

        var normalExpectedKey = TtsAudioKeyBuilder.buildAudioKey("v1", normalText, normalHash);
        var punctExpectedKey = TtsAudioKeyBuilder.buildAudioKey("v1", punctText, punctHash);

        verify(r2StorageService).putBytes(eq(normalExpectedKey), any(byte[].class), anyString());
        verify(r2StorageService).putBytes(eq(punctExpectedKey), any(byte[].class), anyString());
    }

    @Test
    void publishNext_shouldMarkFailedOnError() {
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);
        when(speechWorkerService.generate(any())).thenThrow(new RuntimeException("boom"));

        service.publishNext();

        assertEquals(TtsPublishQueue.STATUS_FAILED, item.getStatus());
        assertNotNull(item.getNextAttemptAt());
        assertTrue(item.getAttemptCount() > 0);
        verify(repository).save(item);
    }

    @Test
    @SuppressWarnings("null")
    void publishNext_shouldDeleteQueueItemOnInvalidSpeechWorkerInputError() {
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);
        when(speechWorkerService.generate(any())).thenThrow(
                new RuntimeException(
                        "Speech worker call failed: 500 INTERNAL_SERVER_ERROR\n"
                                + "Response body: {\"detail\":\"need at least one array to concatenate\"}"
                )
        );

        service.publishNext();

        verify(repository).deleteById(1L);
        verify(repository, never()).save(item);
        assertNull(item.getNextAttemptAt());
        assertEquals(0, item.getAttemptCount());
    }

    @Test
    void publishNext_shouldUseCloudflareWhenProviderIsCloudflareAura2() {
        ReflectionTestUtils.setField(service, "ttsProvider", TtsPublisherService.PROVIDER_CLOUDFLARE_AURA2);
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);
        when(cloudflareAIService.textToSpeech(anyString())).thenReturn("audio-data".getBytes());

        service.publishNext();

        verify(cloudflareAIService, times(2)).textToSpeech(anyString());
        verify(speechWorkerService, never()).generate(any());
        verify(r2StorageService, times(2)).putBytes(anyString(), any(byte[].class), eq("audio/mpeg"));
        verify(repository).deleteById(1L);
    }

    @Test
    void publishNext_shouldMarkFailedWhenCloudflareErrors() {
        ReflectionTestUtils.setField(service, "ttsProvider", TtsPublisherService.PROVIDER_CLOUDFLARE_AURA2);
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);
        when(cloudflareAIService.textToSpeech(anyString()))
                .thenThrow(new RuntimeException("Cloudflare AI call failed: 400"));

        service.publishNext();

        assertEquals(TtsPublishQueue.STATUS_FAILED, item.getStatus());
        assertNotNull(item.getNextAttemptAt());
        assertTrue(item.getAttemptCount() > 0);
        verify(repository).save(item);
    }

    @Test
    void publishNext_shouldDefaultToSpeechWorkerProvider() {
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);

        var response = new SpeechWorkerService.GenerateResponse();
        response.audioBase64 = Base64.getEncoder().encodeToString("hello".getBytes());
        response.mimeType = "audio/mpeg";
        when(speechWorkerService.generate(any(SpeechWorkerService.GenerateRequest.class))).thenReturn(response);

        service.publishNext();

        verify(speechWorkerService, times(2)).generate(any());
        verify(cloudflareAIService, never()).textToSpeech(anyString());
    }

    @Test
    void publishNext_shouldPublishEvenWhenArtifactsExistWhenForceReplaceAudio() {
        var item = createItem();
        item.setForceReplaceAudio(true);

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), anyInt(), any()))
                .thenReturn(List.of(item));

        var response = new SpeechWorkerService.GenerateResponse();
        response.audioBase64 = Base64.getEncoder().encodeToString("hello".getBytes());
        response.mimeType = "audio/mpeg";
        when(speechWorkerService.generate(any(SpeechWorkerService.GenerateRequest.class))).thenReturn(response);

        service.publishNext();

        verify(r2StorageService, never()).exists(anyString());
        verify(r2StorageService, times(2)).putBytes(anyString(), any(byte[].class), anyString());
        verify(repository).deleteById(1L);
    }

    private TtsPublishQueue createItem() {
        var item = new TtsPublishQueue();
        item.setId(1L);
        item.setTtsVersion("v1");
        item.setStatus(TtsPublishQueue.STATUS_PENDING);
        item.setContent("Hello world.");
        item.setCreatedDate(new Date());
        item.setLastUpdatedDate(new Date());
        return item;
    }

}