package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.service.rest.R2StorageService;
import com.esl.service.rest.SpeechWorkerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TtsPublisherServiceTest {

    @Test
    void publishNext_shouldSkipWhenR2NotConfigured() {
        var repository = mock(TtsPublishQueueRepository.class);
        var r2StorageService = mock(R2StorageService.class);
        var speechWorkerService = mock(SpeechWorkerService.class);
        var objectMapper = mock(ObjectMapper.class);
        var service = createService(repository, r2StorageService, speechWorkerService, objectMapper);

        when(r2StorageService.isConfigured()).thenReturn(false);

        service.publishNext();

        verify(repository, never()).findNext(anyList(), any(Date.class), any());
    }

    @Test
    void publishNext_shouldDeleteWhenArtifactsExist() {
        var repository = mock(TtsPublishQueueRepository.class);
        var r2StorageService = mock(R2StorageService.class);
        var speechWorkerService = mock(SpeechWorkerService.class);
        var objectMapper = mock(ObjectMapper.class);
        var service = createService(repository, r2StorageService, speechWorkerService, objectMapper);
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(true);

        service.publishNext();

        verify(repository).delete(item);
        verify(speechWorkerService, never()).generate(any());
    }

    @Test
    void publishNext_shouldPublishAndDeleteWhenMissingArtifacts() throws Exception {
        var repository = mock(TtsPublishQueueRepository.class);
        var r2StorageService = mock(R2StorageService.class);
        var speechWorkerService = mock(SpeechWorkerService.class);
        var objectMapper = mock(ObjectMapper.class);
        var service = createService(repository, r2StorageService, speechWorkerService, objectMapper);
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);
        when(objectMapper.writeValueAsBytes(any(Map.class))).thenReturn(new byte[] {1, 2, 3});

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

        verify(r2StorageService, times(4)).putBytes(anyString(), any(byte[].class), anyString());
        verify(repository).delete(item);
    }

    @Test
    void publishNext_shouldMarkFailedOnError() {
        var repository = mock(TtsPublishQueueRepository.class);
        var r2StorageService = mock(R2StorageService.class);
        var speechWorkerService = mock(SpeechWorkerService.class);
        var objectMapper = mock(ObjectMapper.class);
        var service = createService(repository, r2StorageService, speechWorkerService, objectMapper);
        var item = createItem();

        when(r2StorageService.isConfigured()).thenReturn(true);
        when(repository.findNext(anyList(), any(Date.class), any()))
                .thenReturn(List.of(item));
        when(r2StorageService.exists(anyString())).thenReturn(false);
        when(speechWorkerService.generate(any())).thenThrow(new RuntimeException("boom"));

        service.publishNext();

        assertEquals(TtsPublishQueue.STATUS_FAILED, item.getStatus());
        assertNotNull(item.getNextAttemptAt());
        assertTrue(item.getAttemptCount() > 0);
        verify(repository).save(item);
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

    private TtsPublisherService createService(
            TtsPublishQueueRepository repository,
            R2StorageService r2StorageService,
            SpeechWorkerService speechWorkerService,
            ObjectMapper objectMapper
    ) {
        var service = new TtsPublisherService(repository, r2StorageService, speechWorkerService, objectMapper);
        ReflectionTestUtils.setField(service, "defaultTtsVersion", "v1");
        ReflectionTestUtils.setField(service, "backoffSeconds", 60);
        ReflectionTestUtils.setField(service, "ttsVoice", "af_sarah");
        ReflectionTestUtils.setField(service, "batchSize", 100);
        return service;
    }
}
