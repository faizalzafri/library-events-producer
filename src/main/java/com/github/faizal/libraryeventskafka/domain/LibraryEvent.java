package com.github.faizal.libraryeventskafka.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {

    private String libraryEventId;
    private LibraryEventType libraryEventType;
    @NotNull
    private Book book;
}
