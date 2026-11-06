package com.esl.controller;

import com.esl.dao.MemberDAO;
import com.esl.entity.ImageReport;
import com.esl.service.ImageReportService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageReportController {
    private final ImageReportService imageReportService;
    private final MemberDAO memberDAO;

    public ImageReportController(ImageReportService imageReportService, MemberDAO memberDAO) {
        this.imageReportService = imageReportService;
        this.memberDAO = memberDAO;
    }

    public record ImageReportRequest(String phrase, Integer imageIndex) {}

    public record ImageReportResponse(long id, String phrase, int imageIndex, Long memberId) {
        static ImageReportResponse from(ImageReport report) {
            return new ImageReportResponse(report.getId(), report.getPhrase(), report.getImageIndex(), report.getMemberId());
        }
    }

    @PostMapping("/image-report")
    public ImageReportResponse report(@RequestBody ImageReportRequest request) {
        if (request == null || request.imageIndex() == null) {
            throw new IllegalArgumentException("imageIndex is required");
        }
        var saved = imageReportService.report(request.phrase(), request.imageIndex(), currentMemberId());
        return ImageReportResponse.from(saved);
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
