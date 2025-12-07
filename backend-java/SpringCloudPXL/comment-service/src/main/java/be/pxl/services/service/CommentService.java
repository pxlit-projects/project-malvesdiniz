package be.pxl.services.service;

import be.pxl.services.client.PostClient;
import be.pxl.services.domain.Comment;
import be.pxl.services.domain.dto.CommentRequest;
import be.pxl.services.domain.dto.CommentResponse;
import be.pxl.services.domain.dto.PostResponse;
import be.pxl.services.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements ICommentService{
    private final CommentRepository commentRepository;
    private final PostClient postClient;
    @Override
    public CommentResponse addComment(UUID postId, String author, CommentRequest req) {
        PostResponse post = postClient.getPost(postId);
        if (!post.status().equals("APPROVED")) {
            throw new RuntimeException("Cannot comment on a post that is not published.");
        }
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthor(author);
        comment.setText(req.getText());
        comment.setCreatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return new CommentResponse(
                saved.getId(),
                saved.getPostId(),
                saved.getAuthor(),
                saved.getText(),
                saved.getCreatedAt()
        );
    }
    @Override
    public List<CommentResponse> getComments(UUID postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.getPostId(),
                        c.getAuthor(),
                        c.getText(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public CommentResponse editComment(UUID commentId, String user, CommentRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().equals(user)) {
            throw new RuntimeException("You can edit only your own comments.");
        }

        comment.setText(req.getText());

        Comment saved = commentRepository.save(comment);

        return new CommentResponse(
                saved.getId(),
                saved.getPostId(),
                saved.getAuthor(),
                saved.getText(),
                saved.getCreatedAt()
        );
    }

    @Override
    public void deleteComment(UUID commentId, String user) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().equals(user)) {
            throw new RuntimeException("You can delete only your own comments.");
        }

        commentRepository.delete(comment);
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
