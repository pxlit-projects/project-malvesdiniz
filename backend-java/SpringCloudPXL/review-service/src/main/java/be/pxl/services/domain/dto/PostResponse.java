package be.pxl.services.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String title,
        String content,
        String auteur,
        LocalDateTime createdAt,
        String status,
        String reviewComment
) {}
