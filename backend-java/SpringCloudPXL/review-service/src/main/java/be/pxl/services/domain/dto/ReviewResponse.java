package be.pxl.services.domain.dto;

import be.pxl.services.domain.ReviewStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ReviewResponse {
    private UUID postId;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private ReviewStatus status;
    private String comment;
}

