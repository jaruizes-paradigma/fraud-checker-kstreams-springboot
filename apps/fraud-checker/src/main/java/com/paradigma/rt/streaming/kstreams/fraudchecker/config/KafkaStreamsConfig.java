package com.paradigma.rt.streaming.kstreams.fraudchecker.config;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${app.topics.movements: movements}")
    private String movementsTopic;

    @Value(value = "${app.topics.fraud: fraud-case}")
    private String fraudTopic;

    @Value(value = "${app.session.inactivity-gap: 60}")
    private int sessionInactivityGap;

    @Value(value = "${app.session.check-inactive: 10}")
    private int checkInactiveSessionsInterval;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kafkaStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "fraud-checker");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    FraudCheckerConfig fraudCheckerConfig() {
        return new FraudCheckerConfig(movementsTopic, fraudTopic, sessionInactivityGap, checkInactiveSessionsInterval);
    }
}