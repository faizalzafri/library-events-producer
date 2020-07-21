package com.github.faizal.libraryeventskafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.faizal.libraryeventskafka.domain.Book;
import com.github.faizal.libraryeventskafka.domain.LibraryEvent;
import com.github.faizal.libraryeventskafka.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
class LibraryEventsControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper mapper;

    private Consumer consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> config =
                new HashMap<>(KafkaTestUtils
                        .consumerProps("intg-test", String.valueOf(true), embeddedKafkaBroker));

        consumer = new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);

    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void postLibraryEvent() throws JsonProcessingException {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(libraryEvent, headers);

        //when
        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST, entity, LibraryEvent.class);
        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        LibraryEvent libraryEventFromKafka = mapper.readValue(consumerRecord.value(), LibraryEvent.class);
        assertEquals(libraryEvent, libraryEventFromKafka);

    }

    @Test
    void postLibraryEventSync() {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(libraryEvent, headers);

        //when
        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/libraryevent/sync", HttpMethod.POST, entity, LibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Timeout(15)
    void putLibraryEvent() throws JsonProcessingException {
        //given
        Book book = Book.builder()
                .bookId(111)
                .bookName("Learn Kafka")
                .bookAuthor("Faizal")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId("ASDC565S")
                .libraryEventType(LibraryEventType.UPDATE)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(libraryEvent, headers);

        //when
        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/libraryevent", HttpMethod.PUT, entity, LibraryEvent.class);
        ConsumerRecord<String, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        LibraryEvent libraryEventFromKafka = mapper.readValue(consumerRecord.value(), LibraryEvent.class);
        assertEquals(libraryEvent, libraryEventFromKafka);

    }

    @Test
    public void putLibraryEvent_InvalidLibraryEventID() {
        Book book = Book.builder()
                .bookId(123)
                .bookName("Learn Kafka")
                .bookAuthor("Faizal")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        ResponseEntity<?> responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.PUT, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}