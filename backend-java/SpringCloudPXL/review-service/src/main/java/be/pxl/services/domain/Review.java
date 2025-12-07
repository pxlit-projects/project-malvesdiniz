package be.pxl.services.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID postId;
    private String reviewer;
    private LocalDateTime reviewedAt;
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
    private String comment;
}

