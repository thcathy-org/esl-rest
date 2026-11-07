package com.esl.controller;

import com.esl.dao.MemberDAO;
import com.esl.entity.ImageFlagging;
import com.esl.service.ImageFlaggingService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageFlaggingController {
    private final ImageFlaggingService imageFlaggingService;
    private final MemberDAO memberDAO;

    public ImageFlaggingController(ImageFlaggingService imageFlaggingService, MemberDAO memberDAO) {
        this.imageFlaggingService = imageFlaggingService;
        this.memberDAO = memberDAO;
    }

    public record ImageFlaggingRequest(String phrase, Integer imageIndex) {}

    public record ImageFlaggingResponse(long id, String phrase, int imageIndex, Long memberId) {
        static ImageFlaggingResponse from(ImageFlagging flagging) {
            return new ImageFlaggingResponse(flagging.getId(), flagging.getPhrase(), flagging.getImageIndex(), flagging.getMemberId());
        }
    }

    @PostMapping("/image-flagging")
    public ImageFlaggingResponse flag(@RequestBody ImageFlaggingRequest request) {
        if (request == null || request.imageIndex() == null) {
            throw new IllegalArgumentException("imageIndex is required");
        }
        var saved = imageFlaggingService.flag(request.phrase(), request.imageIndex(), currentMemberId());
        return ImageFlaggingResponse.from(saved);
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
