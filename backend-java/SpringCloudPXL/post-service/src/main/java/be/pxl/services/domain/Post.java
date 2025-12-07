package be.pxl.services.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue
    private UUID id;
    private String title;
    private String content;
    private String auteur;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private PostStatus status;
    private String reviewComment;




}



