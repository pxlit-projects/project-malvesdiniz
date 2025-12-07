package be.pxl.services.domain.dto;

import java.time.LocalDate;

public record FilterDtoRequest(String auteur, String text, LocalDate date) {
}
