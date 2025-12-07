package be.pxl.services.controller;

import be.pxl.services.domain.dto.ReviewRequest;
import be.pxl.services.domain.dto.ReviewResponse;
import be.pxl.services.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService reviewService;

    @PutMapping("/{postId}/approve")
    public ResponseEntity<ReviewResponse> approve(
            @PathVariable UUID postId,
            @RequestHeader("USER") String reviewer,
            @RequestHeader("ROLE") String role
    ) {
        return ResponseEntity.ok(reviewService.approve(postId, reviewer, role));
    }

    @PutMapping("/{postId}/reject")
    public ResponseEntity<ReviewResponse> reject(
            @PathVariable UUID postId,
            @RequestHeader("USER") String reviewer,
            @RequestHeader("ROLE") String role,
            @RequestBody ReviewRequest request
    ) {
        return ResponseEntity.ok(reviewService.reject(postId, request, reviewer, role));
    }
}
