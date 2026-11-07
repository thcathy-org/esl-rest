package com.esl.controller;

import com.esl.entity.FlagImage;
import com.esl.service.FlagImageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlagImageController {
    private final FlagImageService flagImageService;

    public FlagImageController(FlagImageService flagImageService) {
        this.flagImageService = flagImageService;
    }

    public record FlagImageRequest(String phrase, Integer imageIndex) {}

    public record FlagImageResponse(long id, String phrase, int imageIndex, int count) {
        static FlagImageResponse from(FlagImage flaggedImage) {
            return new FlagImageResponse(
                    flaggedImage.getId(),
                    flaggedImage.getPhrase(),
                    flaggedImage.getImageIndex(),
                    flaggedImage.getFlagCount());
        }
    }

    @PostMapping("/flag-image")
    public FlagImageResponse flag(@RequestBody FlagImageRequest request) {
        if (request == null || request.imageIndex() == null) {
            throw new IllegalArgumentException("imageIndex is required");
        }
        var saved = flagImageService.flag(request.phrase(), request.imageIndex());
        return FlagImageResponse.from(saved);
    }
}
