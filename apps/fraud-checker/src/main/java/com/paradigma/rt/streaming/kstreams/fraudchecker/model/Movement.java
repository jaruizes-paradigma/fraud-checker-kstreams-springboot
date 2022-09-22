package com.paradigma.rt.streaming.kstreams.fraudchecker.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Movement {

    String id;
    String card;
    float amount;
    long origin;
    String site;
    String device;
    String createdAt;

    @Override
    public String toString() {
        return "[id='" + id + '\'' +
                ", card='" + card + '\'' +
                ", amount=" + amount +
                ", origin=" + origin +
                ", site='" + site + '\'' +
                ", device='" + device + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ']';
    }
}
