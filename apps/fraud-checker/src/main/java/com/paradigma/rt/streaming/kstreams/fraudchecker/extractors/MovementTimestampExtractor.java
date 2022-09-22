package com.paradigma.rt.streaming.kstreams.fraudchecker.extractors;

import com.paradigma.rt.streaming.kstreams.fraudchecker.model.Movement;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MovementTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long l) {
        Movement movement = (Movement) consumerRecord.value();
        return iso8601ToEpoch(movement.getCreatedAt());
    }

    private long iso8601ToEpoch(String createdAt) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        try {
            return formatter.parse(createdAt).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}