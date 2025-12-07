package be.pxl.services.service;

import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.domain.dto.ReviewRequest;
import be.pxl.services.domain.dto.ReviewResponse;

import java.util.UUID;

public interface IReviewService {
    ReviewResponse approve(UUID postId, String reviewer, String role);
    ReviewResponse reject(UUID postId, ReviewRequest req, String reviewer, String role);
    Iterable<PostResponse> getPendingPosts();
}
