package com.paradigma.rt.streaming.kstreams.fraudcheckergenerator.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MovementGenerator {

    String id;
    String card;
    float amount;
    long origin;
    String site;
    String device;
    String createdAt;

}
