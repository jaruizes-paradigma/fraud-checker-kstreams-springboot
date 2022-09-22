package com.paradigma.rt.streaming.kstreams.fraudchecker.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

import static com.paradigma.rt.streaming.kstreams.fraudchecker.config.FraudCheckerConfig.ONLINE_FRAUD_DESCRIPTION;
import static com.paradigma.rt.streaming.kstreams.fraudchecker.config.FraudCheckerConfig.PHYSICAL_FRAUD_DESCRIPTION;

@Data
public class FraudCase {
    public static final int ONLINE_MOVEMENT = 3;

    private String card;
    private String description;
    private float totalAmount;
    private Set<String> movements;

    public FraudCase(){
        this.totalAmount = 0f;
        this.movements = new HashSet<>();
    }

    public void addMovement(Movement movement){
        this.description = movement.getOrigin() == ONLINE_MOVEMENT ? ONLINE_FRAUD_DESCRIPTION : PHYSICAL_FRAUD_DESCRIPTION;
        this.card = movement.getCard();
        this.movements.add(movement.getId());
        this.totalAmount += movement.getAmount();
    }
}