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

    public record FlagImageRequest(String phrase, Integer imageIndex) {
        public static FlagImageRequest require(FlagImageRequest request) {
            if (request == null || request.imageIndex == null) {
                throw new IllegalArgumentException("imageIndex is required");
            }
            return request;
        }
    }

    public record FlagImageResponse(long id, String phrase, int imageIndex, int count) {
        static FlagImageResponse from(FlagImage flagImage) {
            return new FlagImageResponse(
                    flagImage.getId(),
                    flagImage.getPhrase(),
                    flagImage.getImageIndex(),
                    flagImage.getFlagCount());
        }
    }

    @PostMapping("/flag-image")
    public FlagImageResponse flag(@RequestBody FlagImageRequest request) {
        var flagImageRequest = FlagImageRequest.require(request);
        var flagImage = flagImageService.flag(flagImageRequest.phrase(), flagImageRequest.imageIndex());
        return FlagImageResponse.from(flagImage);
    }
}
