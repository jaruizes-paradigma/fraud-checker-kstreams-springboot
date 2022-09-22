package com.paradigma.rt.streaming.kstreams.fraudchecker.config;

import lombok.Getter;

@Getter
public class FraudCheckerConfig {
    public final static String ONLINE_FRAUD_DESCRIPTION = "ONLINE FRAUD CASE";
    public final static String PHYSICAL_FRAUD_DESCRIPTION = "PHYSICAL FRAUD CASE";

    private String movementsTopic;
    private String fraudTopic;
    private int sessionInactivityGap;
    private int checkInactiveSessionsInterval;

    public FraudCheckerConfig() {}

    public FraudCheckerConfig(String movementsTopic, String fraudTopic, int sessionInactivityGap, int checkInactiveSessionsInterval) {
        this.movementsTopic = movementsTopic;
        this.fraudTopic = fraudTopic;
        this.sessionInactivityGap = sessionInactivityGap;
        this.checkInactiveSessionsInterval = checkInactiveSessionsInterval;
    }
}
