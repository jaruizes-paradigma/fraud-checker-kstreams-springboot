package com.paradigma.rt.streaming.kstreams.fraudcheckergenerator.controller;

import com.paradigma.rt.streaming.kstreams.fraudcheckergenerator.model.MovementGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class MovementGeneratorController {

    private final KafkaTemplate<String, MovementGenerator> kafkaTemplate;

    @Autowired
    public MovementGeneratorController(KafkaTemplate<String, MovementGenerator> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping(value = "/movement", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody MovementGenerator movementGenerator){
        log.info(movementGenerator);
        kafkaTemplate.send("movements", movementGenerator);
        log.info(String.format("Movement %s has been sent to Kafka", movementGenerator.getId()));
    }

}
