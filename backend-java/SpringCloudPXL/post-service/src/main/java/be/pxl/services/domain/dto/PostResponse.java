package be.pxl.services.domain.dto;

import be.pxl.services.domain.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse{
    private UUID id;
    private String title;
    private String content;
    private String auteur;
    private LocalDateTime createdAt;
    private PostStatus status;
    private String reviewComment;
}