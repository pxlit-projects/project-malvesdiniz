package be.pxl.services.service;

import be.pxl.services.domain.dto.CommentRequest;
import be.pxl.services.domain.dto.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface ICommentService {
    CommentResponse addComment(UUID postId, String author, CommentRequest req);
    List<CommentResponse> getComments(UUID postId);
    CommentResponse editComment(UUID commentId, String user, CommentRequest req);
    void deleteComment(UUID commentId, String user);
}
