package be.pxl.services.controller;

import be.pxl.services.domain.dto.CommentRequest;
import be.pxl.services.domain.dto.CommentResponse;
import be.pxl.services.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    private final ICommentService commentService;
    @PostMapping("/{postId}")
    public ResponseEntity<CommentResponse> add(
            @PathVariable UUID postId,
            @RequestHeader("USER") String user,
            @RequestBody CommentRequest request
    ) {
        return ResponseEntity.ok(commentService.addComment(postId, user, request));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponse>> get(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

}
