package be.pxl.services.service;
import be.pxl.services.client.PostClient;
import be.pxl.services.domain.Review;
import be.pxl.services.domain.ReviewStatus;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.domain.dto.ReviewRequest;
import be.pxl.services.domain.dto.ReviewResponse;
import be.pxl.services.messaging.PostEventPublisher;
import be.pxl.services.repository.ReviewRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewService reviewService;

    private ReviewRepository reviewRepository;
    private PostClient postClient;
    private PostEventPublisher publisher;

    @BeforeEach
    void setup() {
        reviewRepository = mock(ReviewRepository.class);
        postClient = mock(PostClient.class);
        publisher = mock(PostEventPublisher.class);

        reviewService = new ReviewService(reviewRepository, postClient, publisher);
    }


    @Test
    void testApprove_ShouldSaveReview_TriggerEvent_AndCallPostClient() {

        UUID postId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        PostResponse post = new PostResponse(
                postId, "t", "c", "a", LocalDateTime.now(), "SUBMITTED", null
        );

        when(postClient.approvePost(postId, "redact")).thenReturn(post);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        ReviewResponse response =
                reviewService.approve(postId, "john", "redact");

        assertNotNull(response);
        assertEquals("john", response.getReviewer());
        assertEquals(ReviewStatus.APPROVED, response.getStatus());

        verify(postClient).approvePost(postId, "redact");

        verify(reviewRepository).save(any(Review.class));

        verify(publisher).sendApprovedEvent(postId);
    }

    @Test
    void testApprove_ShouldFail_WhenRoleIsWrong() {

        UUID postId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.approve(postId, "john", "author")
        );

        verify(reviewRepository, never()).save(any());
        verify(postClient, never()).approvePost(any(), "redact");
        verify(publisher, never()).sendApprovedEvent(any());
    }


    @Test
    void testReject_ShouldSaveReview_TriggerEvent_AndCallPostClient() {

        UUID postId = UUID.randomUUID();

        ReviewRequest request = new ReviewRequest("Too short");

        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        ReviewResponse response =
                reviewService.reject(postId, request, "john", "redact");

        assertNotNull(response);
        assertEquals("john", response.getReviewer());
        assertEquals("Too short", response.getComment());
        assertEquals(ReviewStatus.REJECTED, response.getStatus());

        verify(postClient).rejectPost(postId,"redact", request);

        verify(reviewRepository).save(any(Review.class));

        verify(publisher).sendRejectedEvent(postId, "Too short");
    }

    @Test
    void testReject_ShouldFail_WhenRoleIsWrong() {

        UUID postId = UUID.randomUUID();
        ReviewRequest request = new ReviewRequest("bad");

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.reject(postId, request, "john", "viewer")
        );

        verify(reviewRepository, never()).save(any());
        verify(postClient, never()).rejectPost(any(), "redact",any());
        verify(publisher, never()).sendRejectedEvent(any(), any());
    }


    @Test
    void testGetPendingPosts_ShouldReturnList() {

        PostResponse p1 = new PostResponse(
                UUID.randomUUID(), "t1", "c1", "a1",
                LocalDateTime.now(), "SUBMITTED", null
        );

        when(postClient.getSubmittedPosts()).thenReturn(List.of(p1));

        Iterable<PostResponse> result = reviewService.getPendingPosts();

        assertNotNull(result);
        List<PostResponse> list = (List<PostResponse>) result;

        assertEquals(1, list.size());
        assertEquals("t1", list.get(0).title());

        verify(postClient).getSubmittedPosts();
    }
}
