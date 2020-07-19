package com.github.faizal.libraryeventskafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.faizal.libraryeventskafka.domain.LibraryEvent;
import com.github.faizal.libraryeventskafka.domain.LibraryEventType;
import com.github.faizal.libraryeventskafka.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class LibraryEventsController {

    @Autowired
    private LibraryEventProducer producer;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {

        // invoke kaka producer
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
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

    //PUT ENDPOINT
    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {

        if(Objects.isNull(libraryEvent.getLibraryEventId()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Provide libraryEvent");
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        producer.sendLibraryEvent3(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }
}
