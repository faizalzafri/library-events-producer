package com.github.faizal.libraryeventskafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.faizal.libraryeventskafka.domain.Book;
import com.github.faizal.libraryeventskafka.domain.LibraryEvent;
import com.github.faizal.libraryeventskafka.domain.LibraryEventType;
import com.github.faizal.libraryeventskafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
class LibraryEventsControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LibraryEventProducer producer;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void postLibraryEvent() throws Exception {
        //given
        Book book = Book.builder()
                .bookId(101)
                .bookName("Learn Kafka")
                .bookAuthor("Faizal")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();

        String payload = mapper.writeValueAsString(libraryEvent);

        //when
        when(producer.sendLibraryEvent3(libraryEvent)).thenReturn(null);

        //then
        mockMvc.perform(
                post("/v1/libraryevent")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    }

    @Test
    void postLibraryEvent_4xx() throws Exception {
        //given
       LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(null)
                .build();

       String errorMessage = "book-must not be null";

        String payload = mapper.writeValueAsString(libraryEvent);

        //when
        when(producer.sendLibraryEvent3(libraryEvent)).thenReturn(null);

        //then
        mockMvc.perform(
                post("/v1/libraryevent")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(errorMessage));

    }

    @Test
    void updateLibraryEvent() throws Exception {
        //given
        Book book = Book.builder()
                .bookId(101)
                .bookName("Learn Kafka")
                .bookAuthor("Faizal")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId("ASDC565S")
                .libraryEventType(LibraryEventType.UPDATE)
                .book(book)
                .build();

        String payload = mapper.writeValueAsString(libraryEvent);

        //when
        when(producer.sendLibraryEvent3(libraryEvent)).thenReturn(null);

        //then
        mockMvc.perform(
                put("/v1/libraryevent")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void updateLibraryEvent_withNullLibraryEventId() throws Exception {
        //given
        Book book = Book.builder()
                .bookId(101)
                .bookName("Learn Kafka")
                .bookAuthor("Faizal")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.UPDATE)
                .book(book)
                .build();

        String payload = mapper.writeValueAsString(libraryEvent);

        String errorMessage = "Provide libraryEvent";

        //when
        when(producer.sendLibraryEvent3(libraryEvent)).thenReturn(null);

        //then
        mockMvc.perform(
                put("/v1/libraryevent")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));

    }
}