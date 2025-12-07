package be.pxl.services.service;


import be.pxl.services.domain.Post;
import be.pxl.services.domain.PostStatus;
import be.pxl.services.domain.dto.FilterDtoRequest;
import be.pxl.services.domain.dto.PostRequest;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.repository.PostRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate_ShouldCreatePost_WhenRoleIsRedact() {
        PostRequest req = new PostRequest("T", "C");

        Post saved = new Post(
                UUID.randomUUID(),
                "T",
                "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );

        when(postRepository.save(any())).thenReturn(saved);

        PostResponse response = postService.create(req, "maria", "redact");

        assertNotNull(response);
        assertEquals("T", response.getTitle());
        assertEquals(PostStatus.DRAFT, response.getStatus());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void testCreate_ShouldFail_WhenRoleIsNotRedact() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.create(new PostRequest("T", "C"), "maria", "viewer")
        );

        assertEquals("Only redact can make posts", ex.getMessage());
        verify(postRepository, never()).save(any());
    }


    @Test
    void testUpdateDraft_ShouldUpdate_WhenValid() {
        UUID id = UUID.randomUUID();

        Post existing = new Post(
                id,
                "Old",
                "OldC",
                "maria",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(existing));
        when(postRepository.save(any())).thenReturn(existing);

        PostRequest update = new PostRequest("New", "NewC");

        PostResponse result = postService.updateDraft(id, update, "maria", "redact");

        assertEquals("New", result.getTitle());
        assertEquals("NewC", result.getContent());
        verify(postRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateDraft_ShouldFail_WhenPostNotFound() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.updateDraft(UUID.randomUUID(), new PostRequest("X", "Y"), "maria", "redact")
        );

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void testUpdateDraft_ShouldFail_WhenNotAuthor() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id,
                "T", "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.updateDraft(id, new PostRequest("X", "Y"), "john", "redact")
        );

        assertEquals("Only the auteur can make changes on the draft", ex.getMessage());
    }

    @Test
    void testUpdateDraft_ShouldFail_WhenStatusNotDraft() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id,
                "T", "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.APPROVED,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.updateDraft(id, new PostRequest("X", "Y"), "maria", "redact")
        );

        assertEquals("Only drafts can be edited", ex.getMessage());
    }


    @Test
    void testSubmitPost_ShouldSubmit_WhenValid() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenReturn(post);

        PostResponse result = postService.submitPost(id, "maria", "redact");

        assertEquals(PostStatus.SUBMITTED, result.getStatus());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testSubmitPost_ShouldFail_WhenRoleWrong() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.submitPost(UUID.randomUUID(), "maria", "viewer")
        );

        assertEquals("Only redact can submit drafts", ex.getMessage());
    }

    @Test
    void testSubmitPost_ShouldFail_WhenNotAuthor() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.submitPost(id, "john", "redact")
        );

        assertEquals("Only the auteur can submit the draft", ex.getMessage());
    }

    @Test
    void testSubmitPost_ShouldFail_WhenStatusNotDraft() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C",
                "maria",
                LocalDateTime.now(),
                PostStatus.APPROVED,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.submitPost(id, "maria", "redact")
        );

        assertEquals("Only drafts can be submitted", ex.getMessage());
    }

    @Test
    void testApprove_ShouldApprove_WhenSubmitted() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C",
                "mar",
                LocalDateTime.now(),
                PostStatus.SUBMITTED,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        PostResponse result = postService.approve(id, "reviewer");

        assertEquals(PostStatus.APPROVED, result.getStatus());
    }

    @Test
    void testApprove_ShouldFail_WhenNotSubmitted() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C",
                "mar",
                LocalDateTime.now(),
                PostStatus.DRAFT,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.approve(id, "reviewer")
        );

        assertEquals("Only submitted posts can be approved.", ex.getMessage());
    }


    @Test
    void testReject_ShouldReject_WhenSubmitted() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C",
                "mar",
                LocalDateTime.now(),
                PostStatus.SUBMITTED,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        PostResponse result = postService.reject(id, "reviewer","Bad");

        assertEquals(PostStatus.REJECTED, result.getStatus());
        assertEquals("Bad", result.getReviewComment());
    }

    @Test
    void testReject_ShouldFail_WhenNotSubmitted() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C",
                "mar",
                LocalDateTime.now(),
                PostStatus.APPROVED,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.reject(id, "reviewer", "Bad")
        );

        assertEquals("Only submitted posts can be rejected.", ex.getMessage());
    }


    @Test
    void testGetById_ShouldReturnPost_WhenExists() {
        UUID id = UUID.randomUUID();

        Post p = new Post(
                id, "T", "C", "mar",
                LocalDateTime.now(),
                PostStatus.APPROVED,
                null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(p));

        PostResponse resp = postService.getById(id);

        assertEquals("T", resp.getTitle());
    }

    @Test
    void testGetById_ShouldFail_WhenNotFound() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.getById(UUID.randomUUID())
        );

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void testGetSubmittedPosts_ShouldReturnList() {
        List<Post> posts = List.of(
                new Post(UUID.randomUUID(), "A", "B", "mar",
                        LocalDateTime.now(), PostStatus.SUBMITTED, null)
        );

        when(postRepository.findAllByStatus(PostStatus.SUBMITTED)).thenReturn(posts);

        List<PostResponse> result = postService.getSubmittedPosts();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getTitle());
    }


    @Test
    void testGetAllPublished_ShouldReturnList() {
        FilterDtoRequest filter = new FilterDtoRequest(null, null, null);

        List<Post> posts = List.of(
                new Post(UUID.randomUUID(), "T", "C", "mar",
                        LocalDateTime.now(), PostStatus.APPROVED, null)
        );

        when(postRepository.findAll(any(Specification.class))).thenReturn(posts);

        List<PostResponse> result = postService.getAllPostsPublished(filter);

        assertEquals(1, result.size());
        assertEquals(PostStatus.APPROVED, result.get(0).getStatus());
    }
    @Test
    void testDeletePost_ShouldDelete_WhenRoleIsRedact() {
        UUID id = UUID.randomUUID();

        Post post = new Post(
                id, "T", "C", "maria",
                LocalDateTime.now(), PostStatus.DRAFT, null
        );

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        postService.deletePost(id, "maria", "redact");

        verify(postRepository, times(1)).delete(post);
    }
    @Test
    void testDeletePost_ShouldFail_WhenRoleWrong() {

        UUID id = UUID.randomUUID();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.deletePost(id, "maria", "viewer")
        );

        assertEquals("Only redact can delete posts", ex.getMessage());
        verify(postRepository, never()).delete(any(Post.class));
    }
    @Test
    void testDeletePost_ShouldFail_WhenPostNotFound() {

        when(postRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                postService.deletePost(UUID.randomUUID(), "maria", "redact")
        );

        assertEquals("Post not found", ex.getMessage());
        verify(postRepository, never()).delete(any(Post.class));
    }
}
