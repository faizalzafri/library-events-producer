package com.github.faizal.libraryeventskafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.faizal.libraryeventskafka.domain.LibraryEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class LibraryEventProducer {

    @Autowired
    private KafkaTemplate<Integer, String> template;

    @Autowired
    private ObjectMapper mapper;

    @SneakyThrows
    public void sendLibraryEvent(LibraryEvent libraryEvent) {

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

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Not sent. Root cause: {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in onFailure. {}",throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Sent: {} - {} : Partition: {}",key,value,result.getRecordMetadata().partition());
    }
}
