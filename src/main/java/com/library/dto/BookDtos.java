package com.library.dto;

import com.library.entity.Book;
import jakarta.validation.constraints.*;

public final class BookDtos {
    private BookDtos() {}

    public record Request(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 255) String author,
            @Size(max = 20) @Pattern(regexp = "^[0-9Xx-]*$", message = "ISBN: только цифры, X и дефис") String isbn,
            @Min(1000) @Max(2100) Integer publicationYear,
            @NotNull @Min(0) @Max(10000) Integer totalCopies) {}

    public record Response(Long id, String title, String author, String isbn,
                           Integer publicationYear, int totalCopies, int availableCopies) {
        public static Response from(Book b) {
            return new Response(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getPublicationYear(), b.getTotalCopies(), b.getAvailableCopies());
        }
    }
}
