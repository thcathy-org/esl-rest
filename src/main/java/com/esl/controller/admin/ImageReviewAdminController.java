package com.esl.controller.admin;

import com.esl.controller.ImageReportController.ImageReportRequest;
import com.esl.service.ImageReportService;
import com.esl.service.ImageReportService.ImageReportGroup;
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
    private final ImageReportService imageReportService;
    private final ObjectMapper objectMapper;
    private final String htmlTemplate;

    public ImageReviewAdminController(ImageReportService imageReportService, ObjectMapper objectMapper) throws IOException {
        this.imageReportService = imageReportService;
        this.objectMapper = objectMapper;
        try (var in = getClass().getResourceAsStream("/admin/image-review.html")) {
            if (in == null) throw new IllegalStateException("Missing admin/image-review.html");
            this.htmlTemplate = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> page() throws JsonProcessingException {
        var html = htmlTemplate.replace("__IMAGES_HOST__", objectMapper.writeValueAsString(imageReportService.imagesHost()));
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @GetMapping("/reports")
    public List<ImageReportGroup> reports() {
        return imageReportService.openGroups();
    }

    @PostMapping("/remove")
    public ResponseEntity<Void> remove(@RequestBody ImageReportRequest request) {
        if (request == null || request.imageIndex() == null) {
            throw new IllegalArgumentException("imageIndex is required");
        }
        imageReportService.remove(request.phrase(), request.imageIndex());
        return ResponseEntity.noContent().build();
    }
}
