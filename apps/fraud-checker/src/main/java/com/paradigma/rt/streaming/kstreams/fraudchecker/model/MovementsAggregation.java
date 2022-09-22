package com.paradigma.rt.streaming.kstreams.fraudchecker.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class MovementsAggregation {

    private long id;
    private String card;
    private String lastMovementTimestamp;
    private Set<Movement> movements;

    public MovementsAggregation(){
        this.id = Instant.now().getEpochSecond();
        this.movements = new HashSet<>();
        log.info("Created movement aggregation with ID: {}", id);
    }

    public void addMovement(Movement movement){
        this.card = movement.getCard();
        this.movements.add(movement);
        this.lastMovementTimestamp = movement.getCreatedAt();
        log.info("[ID: {}] Added movement: {}", id, movement);
    }

    @Override
    public String toString() {
        return "MovementsAggregation{" +
                "card='" + card + '\'' +
                ", lastMovementTimestamp='" + lastMovementTimestamp + '\'' +
                ", movements=" + movements +
                '}';
    }
}