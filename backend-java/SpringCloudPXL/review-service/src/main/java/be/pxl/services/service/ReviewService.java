package be.pxl.services.service;

import be.pxl.services.domain.dto.ReviewRequest;
import be.pxl.services.domain.dto.ReviewResponse;
import be.pxl.services.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService{
    private final ReviewRepository reviewRepository;
    @Override
    public ReviewResponse approve(UUID postId, String reviewer, String role) {
        return null;
    }
    @Override
    public ReviewResponse reject(UUID postId, ReviewRequest req, String reviewer, String role) {
        return null;
    }
}
