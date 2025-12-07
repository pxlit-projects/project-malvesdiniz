package be.pxl.services.service;

import be.pxl.services.client.PostClient;
import be.pxl.services.domain.Review;
import be.pxl.services.domain.ReviewStatus;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.domain.dto.ReviewRequest;
import be.pxl.services.domain.dto.ReviewResponse;
import be.pxl.services.messaging.PostEventPublisher;
import be.pxl.services.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService implements IReviewService{
    private final ReviewRepository reviewRepository;
    private final PostClient postClient;
    private final PostEventPublisher publisher;

    @Override
    public ReviewResponse approve(UUID postId, String redact, String role) {
        if (!"redact".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Only redacts can approve posts.");
        }
        PostResponse updatedPost = postClient.approvePost(postId, redact);
        Review review = new Review();
        review.setPostId(postId);
        review.setReviewer(redact);
        review.setReviewedAt(LocalDateTime.now());
        review.setStatus(ReviewStatus.APPROVED);

        Review saved = reviewRepository.save(review);
        log.info("EVENT SENT: Post {} APPROVED by reviewer {} at {}",
                postId, redact, LocalDateTime.now());
        publisher.sendApprovedEvent(postId);
        return new ReviewResponse(
                saved.getPostId(),
                saved.getReviewer(),
                saved.getReviewedAt(),
                saved.getStatus(),
                saved.getComment()
        );
    }
    @Override
    public ReviewResponse reject(UUID postId, ReviewRequest req, String redact, String role) {

        if (!"redact".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Only redacts can reject posts.");
        }

        postClient.rejectPost(postId, redact, req);

        Review review = new Review();
        review.setPostId(postId);
        review.setReviewer(redact);
        review.setReviewedAt(LocalDateTime.now());
        review.setStatus(ReviewStatus.REJECTED);
        review.setComment(req.getComment());

        reviewRepository.save(review);

        log.info("EVENT SENT: Post {} REJECTED by redact {} at {} with comment '{}'",
                postId, redact, LocalDateTime.now(), req.getComment());
        publisher.sendRejectedEvent(postId, req.getComment());
        return new ReviewResponse(
                review.getPostId(),
                review.getReviewer(),
                review.getReviewedAt(),
                review.getStatus(),
                review.getComment()
        );
    }
    public Iterable<PostResponse> getPendingPosts() {
        return postClient.getSubmittedPosts();
    }
}
