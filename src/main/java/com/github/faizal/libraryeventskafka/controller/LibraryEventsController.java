package com.github.faizal.libraryeventskafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.faizal.libraryeventskafka.domain.LibraryEvent;
import com.github.faizal.libraryeventskafka.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class LibraryEventsController {

    @Autowired
    private LibraryEventProducer producer;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {

        // invoke kaka producer
        producer.sendLibraryEvent3(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PostMapping("/v1/libraryevent/sync")
    public ResponseEntity<LibraryEvent> postLibraryEventSync(@RequestBody LibraryEvent libraryEvent) throws InterruptedException, ExecutionException, JsonProcessingException {

        // invoke kaka producer
        SendResult<Integer, String> result = producer.sendLibraryEventSync(libraryEvent);
        log.info("Result: {}", result.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
