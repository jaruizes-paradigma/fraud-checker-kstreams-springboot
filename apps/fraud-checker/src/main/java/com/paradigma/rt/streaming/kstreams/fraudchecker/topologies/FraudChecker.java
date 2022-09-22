package com.paradigma.rt.streaming.kstreams.fraudchecker.topologies;

import com.paradigma.rt.streaming.kstreams.fraudchecker.config.FraudCheckerConfig;
import com.paradigma.rt.streaming.kstreams.fraudchecker.extractors.MovementTimestampExtractor;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.FraudCase;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.Movement;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.MovementsAggregation;
import com.paradigma.rt.streaming.kstreams.fraudchecker.serializers.JsonDeserializer;
import com.paradigma.rt.streaming.kstreams.fraudchecker.serializers.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
public class FraudChecker {

    public static final int ONLINE_MOVEMENT = 3;
    private final FraudCheckerConfig config;

    @Autowired
    public FraudChecker(FraudCheckerConfig config) {
        this.config = config;
    }

    private final Serde<Movement> movementSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(Movement.class));
    private final Serde<FraudCase> fraudSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(FraudCase.class));
    private final Serde<MovementsAggregation> movementsAggregationSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(MovementsAggregation.class));

    @Autowired
    public void buildTopology(StreamsBuilder streamsBuilder) {

        // categorize movements
        Map<String, KStream<String, Movement>> movementTypes = streamsBuilder
                .stream(config.getMovementsTopic(), Consumed.with(Serdes.String(), movementSerde).withTimestampExtractor(new MovementTimestampExtractor()))
                .map((k,v) -> KeyValue.pair(v.getCard(), v))
                .peek((k,v) -> log.debug("Received movement: " + v))
                .split(Named.as("type-"))
                .branch((k,v) -> v.getOrigin() == ONLINE_MOVEMENT, Branched.as("online"))
                .defaultBranch(Branched.as("physical"));


        KStream<String, FraudCase> onlineFraudMovements = movementTypes.get("type-online")
                .peek((k,v) -> log.debug("[ONLINE BRANCH] Processing movement: " + v))
                .groupByKey(Grouped.with(Serdes.String(), movementSerde))
                .windowedBy(SessionWindows.ofInactivityGapWithNoGrace(Duration.ofSeconds(config.getSessionInactivityGap())))
                .aggregate(
                        MovementsAggregation::new,
                        (k, movement, movementsAggregation) -> FraudCheckerUtils.aggMovement(movement, movementsAggregation),
                        (k, i1, i2) -> FraudCheckerUtils.aggMerge(i1, i2),
                        Materialized.with(Serdes.String(), movementsAggregationSerde)
                )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()))
                .toStream()
                .filter((k,v) -> FraudCheckerUtils.isOnlineFraud(v))
                .map((k,v) -> KeyValue.pair(k.key(), FraudCheckerUtils.movementsAggregationToFraudCase(v)))
                .peek((k,v) -> log.debug("Online fraud case detected associated to card: {}", v.getCard()));

        KStream<String, FraudCase> physicalFraudMovements = movementTypes.get("type-physical")
                .peek((k,v) -> log.debug ("[PHYSICAL BRANCH] Processing movement: " + v))
                .groupByKey(Grouped.with(Serdes.String(), movementSerde))
                .windowedBy(SessionWindows.ofInactivityGapWithNoGrace(Duration.ofSeconds(config.getSessionInactivityGap())))
                .aggregate(
                        MovementsAggregation::new,
                        (k, movement, movementsAggregation) -> FraudCheckerUtils.aggMovement(movement, movementsAggregation),
                        (k, i1, i2) -> FraudCheckerUtils.aggMerge(i1, i2),
                        Materialized.with(Serdes.String(), movementsAggregationSerde)
                )
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()))
                .toStream()
                .filter((k,v) -> FraudCheckerUtils.isPhysicalFraud(v))
                .map((k,v) -> KeyValue.pair(k.key(), FraudCheckerUtils.movementsAggregationToFraudCase(v)))
                .peek((k,v) -> log.debug("Physical fraud case detected associated to card: {}", v.getCard()));

        // merge both types
        KStream<String, FraudCase> fraudMovements = onlineFraudMovements.merge(physicalFraudMovements);
        fraudMovements.to(config.getFraudTopic(), Produced.with(Serdes.String(), fraudSerde));

    }

}
