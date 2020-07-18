package com.github.faizal.libraryeventskafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Profile("local")
@Configuration
public class AutoTopicConfig {

    /*
    * Kafka Admin uses this bean to create a new topic.
    * However, this is not a recommended approach.
    * */
    @Bean
    public NewTopic newTopic() {
        return TopicBuilder.name("library-events")
                .partitions(3)
                .replicas(3)
                .build();
    }
}
