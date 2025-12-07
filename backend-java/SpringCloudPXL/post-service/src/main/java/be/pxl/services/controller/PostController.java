package be.pxl.services.controller;

import be.pxl.services.domain.dto.PostRequest;
import be.pxl.services.domain.dto.FilterDtoRequest;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.domain.dto.ReviewDecisionRequest;
import be.pxl.services.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {
    private final IPostService postService;
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAll(){
        return ResponseEntity.ok(postService.getAll());
    }
    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody PostRequest request, @RequestHeader("USER") String user,
                                               @RequestHeader("ROLE") String role){
        PostResponse created = postService.create(request,user, role);

        return ResponseEntity.status(201).body(created);
    }
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updateDraft(@PathVariable UUID id, @RequestBody PostRequest request,@RequestHeader("USER") String user,
                                                    @RequestHeader("ROLE") String role){
        PostResponse updated = postService.updateDraft(id, request, user, role);

        return ResponseEntity.ok(updated);
    }
    @PutMapping("/{id}/submit")
    public ResponseEntity<PostResponse> submitPost(@PathVariable UUID id, @RequestHeader("USER") String user,
                                                   @RequestHeader("ROLE") String role){
        return ResponseEntity.ok(postService.submitPost(id, user, role));
    }
    @GetMapping("/published")
    public ResponseEntity<List<PostResponse>> getAllPublishedPosts(FilterDtoRequest filter){
        return ResponseEntity.ok(postService.getAllPostsPublished(filter));
    }
    @PutMapping("/{id}/approve")
    public ResponseEntity<PostResponse> approve(
            @PathVariable UUID id, @RequestHeader("USER") String reviewer
    ) {

        return ResponseEntity.ok(postService.approve(id, reviewer));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<PostResponse> reject(
            @PathVariable UUID id, @RequestHeader("USER") String reviewer,
            @RequestBody ReviewDecisionRequest request
    ) {

        return ResponseEntity.ok(postService.reject(id, reviewer, request.comment()));
    }
    @GetMapping("/submitted")
    public ResponseEntity<List<PostResponse>> getSubmittedPosts() {
        return ResponseEntity.ok(postService.getSubmittedPosts());
    }
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getById(id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID id,
            @RequestHeader("USER") String user,
            @RequestHeader("ROLE") String role) {

        postService.deletePost(id, user, role);
        return ResponseEntity.noContent().build();
    }
}
