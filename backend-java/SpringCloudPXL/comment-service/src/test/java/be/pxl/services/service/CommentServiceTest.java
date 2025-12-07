package be.pxl.services.service;


import be.pxl.services.client.PostClient;
import be.pxl.services.domain.Comment;
import be.pxl.services.domain.dto.CommentRequest;
import be.pxl.services.domain.dto.CommentResponse;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.repository.CommentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostClient postClient;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddComment_ShouldAdd_WhenPostApproved() {
        UUID postId = UUID.randomUUID();
        CommentRequest req = new CommentRequest("Nice post!");

        PostResponse post = new PostResponse(
                postId, "T", "C", "maria",
                LocalDateTime.now(), "APPROVED"
        );

        when(postClient.getPost(postId)).thenReturn(post);

        Comment saved = new Comment();
        saved.setId(UUID.randomUUID());
        saved.setPostId(postId);
        saved.setAuthor("john");
        saved.setText("Nice post!");
        saved.setCreatedAt(LocalDateTime.now());

        when(commentRepository.save(any())).thenReturn(saved);

        CommentResponse result = commentService.addComment(postId, "john", req);

        assertEquals("Nice post!", result.getText());
        assertEquals("john", result.getAuthor());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void testAddComment_ShouldFail_WhenPostNotApproved() {
        UUID postId = UUID.randomUUID();

        PostResponse post = new PostResponse(
                postId, "T", "C", "maria",
                LocalDateTime.now(), "DRAFT"
        );

        when(postClient.getPost(postId)).thenReturn(post);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                commentService.addComment(postId, "john", new CommentRequest("Hello"))
        );

        assertEquals("Cannot comment on a post that is not published.", ex.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void testAddComment_ShouldFail_WhenPostIsNull() {
        when(postClient.getPost(any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                commentService.addComment(UUID.randomUUID(), "john", new CommentRequest("Hello"))
        );

        assertEquals("Cannot invoke \"be.pxl.services.domain.dto.PostResponse.status()\" because \"post\" is null",
                ex.getMessage());
    }

    @Test
    void testGetComments_ShouldReturnList() {
        UUID postId = UUID.randomUUID();

        Comment c1 = new Comment();
        c1.setId(UUID.randomUUID());
        c1.setPostId(postId);
        c1.setAuthor("john");
        c1.setText("First");
        c1.setCreatedAt(LocalDateTime.now());

        Comment c2 = new Comment();
        c2.setId(UUID.randomUUID());
        c2.setPostId(postId);
        c2.setAuthor("mary");
        c2.setText("Second");
        c2.setCreatedAt(LocalDateTime.now().plusMinutes(1));

        List<Comment> list = List.of(c1, c2);

        when(commentRepository.findByPostIdOrderByCreatedAtAsc(postId)).thenReturn(list);

        List<CommentResponse> result = commentService.getComments(postId);

        assertEquals(2, result.size());
        assertEquals("First", result.get(0).getText());
        assertEquals("Second", result.get(1).getText());
    }


    @Test
    void testEditComment_ShouldEdit_WhenUserIsAuthor() {
        UUID commentId = UUID.randomUUID();

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setAuthor("john");
        existing.setText("Old");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));
        when(commentRepository.save(any())).thenReturn(existing);

        CommentRequest req = new CommentRequest("New text");

        CommentResponse result = commentService.editComment(commentId, "john", req);

        assertEquals("New text", result.getText());
        verify(commentRepository, times(1)).save(existing);
    }

    @Test
    void testEditComment_ShouldFail_WhenNotAuthor() {
        UUID commentId = UUID.randomUUID();

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setAuthor("john");
        existing.setText("Old");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                commentService.editComment(commentId, "mary", new CommentRequest("New"))
        );

        assertEquals("You can edit only your own comments.", ex.getMessage());
    }

    @Test
    void testEditComment_ShouldFail_WhenNotFound() {
        when(commentRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                commentService.editComment(UUID.randomUUID(), "john", new CommentRequest("X"))
        );

        assertEquals("Comment not found", ex.getMessage());
    }


    @Test
    void testDeleteComment_ShouldDelete_WhenUserIsAuthor() {
        UUID commentId = UUID.randomUUID();

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setAuthor("john");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));

        commentService.deleteComment(commentId, "john");

        verify(commentRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteComment_ShouldFail_WhenNotAuthor() {
        UUID commentId = UUID.randomUUID();

        Comment existing = new Comment();
        existing.setId(commentId);
        existing.setAuthor("john");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                commentService.deleteComment(commentId, "mary")
        );

        assertEquals("You can delete only your own comments.", ex.getMessage());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void testDeleteComment_ShouldFail_WhenNotFound() {
        when(commentRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                commentService.deleteComment(UUID.randomUUID(), "john")
        );

        assertEquals("Comment not found", ex.getMessage());
    }
}