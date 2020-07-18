package com.github.faizal.libraryeventskafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.faizal.libraryeventskafka.domain.LibraryEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {

    private final static String _TOPIC = "library-events";

    @Autowired
    private KafkaTemplate<Integer, String> template;

    @Autowired
    private ObjectMapper mapper;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);
//        batching and/or linger.ms in effect, thus returning future
        ListenableFuture<SendResult<Integer, String>> future = template.sendDefault(key, value);
        future.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    public void sendLibraryEvent2(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);
        ListenableFuture<SendResult<Integer, String>> future = template.sendDefault(key, value);
        future.addCallback(result -> handleSuccess(key, value, result), ex -> handleFailure(key, value, ex));
    }

    public void sendLibraryEvent3(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);
        ProducerRecord<Integer,String> record = buildProducerRecord(key,value,_TOPIC);
        ListenableFuture<SendResult<Integer, String>> future = template.send(record);
        future.addCallback(result -> handleSuccess(key, value, result), ex -> handleFailure(key, value, ex));
    }

    public SendResult<Integer, String> sendLibraryEventSync(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> result = null;
        try {
            result = template.sendDefault(key, value).get();
        } catch (ExecutionException | InterruptedException ex) {
            log.error("ExecutionException/InterruptedException. Not sent. Root cause: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("ExecutionException/InterruptedException. Not sent. Root cause: {}", ex.getMessage());
            throw ex;
        }
        return result;
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Not sent. Root cause: {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in onFailure. {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Sent: {} - {} : Partition: {}", key, value, result.getRecordMetadata().partition());
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {
        List<Header> headers = Arrays.asList(new RecordHeader("event-source", "handheld-scanner".getBytes()));
        return new ProducerRecord<>(topic,null, Instant.now().getEpochSecond(),key,value,headers);
    }
}
