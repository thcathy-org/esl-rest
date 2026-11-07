package com.esl.controller.admin;

import com.esl.controller.ImageFlaggingController.ImageFlaggingRequest;
import com.esl.service.ImageFlaggingService;
import com.esl.service.ImageFlaggingService.ImageFlaggingGroup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/admin/image-review")
public class ImageReviewAdminController {
    private final ImageFlaggingService imageFlaggingService;
    private final ObjectMapper objectMapper;
    private final String htmlTemplate;

    public ImageReviewAdminController(ImageFlaggingService imageFlaggingService, ObjectMapper objectMapper) throws IOException {
        this.imageFlaggingService = imageFlaggingService;
        this.objectMapper = objectMapper;
        try (var in = getClass().getResourceAsStream("/admin/image-review.html")) {
            if (in == null) throw new IllegalStateException("Missing admin/image-review.html");
            this.htmlTemplate = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> page() throws JsonProcessingException {
        var html = htmlTemplate.replace("__IMAGES_HOST__", objectMapper.writeValueAsString(imageFlaggingService.imagesHost()));
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @GetMapping("/flaggings")
    public List<ImageFlaggingGroup> flaggings() {
        return imageFlaggingService.openGroups();
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> remove(@RequestBody ImageFlaggingRequest request) {
        if (request == null || request.imageIndex() == null) {
            throw new IllegalArgumentException("imageIndex is required");
        }
        imageFlaggingService.remove(request.phrase(), request.imageIndex());
        return ResponseEntity.noContent().build();
    }
}
