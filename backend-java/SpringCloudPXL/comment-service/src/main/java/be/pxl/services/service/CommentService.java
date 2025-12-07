package be.pxl.services.service;

import be.pxl.services.domain.Comment;
import be.pxl.services.domain.dto.CommentRequest;
import be.pxl.services.domain.dto.CommentResponse;
import be.pxl.services.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements ICommentService{
    private final CommentRepository commentRepository;
//    private final PostClient postClient;
    @Override
    public CommentResponse addComment(UUID postId, String author, CommentRequest req) {
        return null;
    }
    @Override
    public List<CommentResponse> getComments(UUID postId) {
        return null;
    }
    private CommentResponse toDTO(Comment c) {
        CommentResponse commentResponse = CommentResponse.builder()
                .author(c.getAuthor())
                .text(c.getText())
                .postId(c.getPostId())
                .createdAt(c.getCreatedAt())
                .build();
        return commentResponse;
    }
}
