package com.esl.controller;

import com.esl.dao.MemberDAO;
import com.esl.entity.FlagImage;
import com.esl.service.FlagImageService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlagImageController {
    private final FlagImageService flagImageService;
    private final MemberDAO memberDAO;

    public FlagImageController(FlagImageService flagImageService, MemberDAO memberDAO) {
        this.flagImageService = flagImageService;
        this.memberDAO = memberDAO;
    }

    public record FlagImageRequest(String phrase, Integer imageIndex) {}

    public record FlagImageResponse(long id, String phrase, int imageIndex, Long memberId) {
        static FlagImageResponse from(FlagImage flaggedImage) {
            return new FlagImageResponse(flaggedImage.getId(), flaggedImage.getPhrase(), flaggedImage.getImageIndex(), flaggedImage.getMemberId());
        }
    }

    @PostMapping("/flag-image")
    public FlagImageResponse flag(@RequestBody FlagImageRequest request) {
        if (request == null || request.imageIndex() == null) {
            throw new IllegalArgumentException("imageIndex is required");
        }
        var saved = flagImageService.flag(request.phrase(), request.imageIndex(), currentMemberId());
        return FlagImageResponse.from(saved);
    }

    private Long currentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        var name = authentication.getName();
        if (name == null || name.isBlank()) {
            return null;
        }
        return memberDAO.getMemberByEmail(name).map(member -> member.getId()).orElse(null);
    }
}
