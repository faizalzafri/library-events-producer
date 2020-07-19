package com.github.faizal.libraryeventskafka.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {

    @NotNull
    private Integer bookId;
    @NotNull
    @NotBlank
    private String bookName;
    @NotNull
    private String bookAuthor;
}
