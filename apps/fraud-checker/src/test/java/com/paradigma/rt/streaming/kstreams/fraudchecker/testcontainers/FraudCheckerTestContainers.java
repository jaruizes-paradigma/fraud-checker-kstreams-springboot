package com.paradigma.rt.streaming.kstreams.fraudchecker.testcontainers;

import com.paradigma.rt.streaming.kstreams.fraudchecker.config.FraudCheckerConfig;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.FraudCase;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.Movement;
import com.paradigma.rt.streaming.kstreams.fraudchecker.serializers.JsonDeserializer;
import com.paradigma.rt.streaming.kstreams.fraudchecker.serializers.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Testcontainers
@SpringBootTest
@DirtiesContext
public class FraudCheckerTestContainers {
    public static final int ATM_MOVEMENT = 1;
    public static final int MERCHANT_MOVEMENT = 2;
    public static final int ONLINE_MOVEMENT = 3;

    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));

    private final BlockingQueue<FraudCase> output = new LinkedBlockingQueue<>();

    private DateTimeFormatter formatter;

    private KafkaProducer<String, Movement> producer;
    private KafkaMessageListenerContainer<String, FraudCase> consumer;


    @Autowired
    FraudCheckerConfig fraudCheckerConfig;

    public FraudCheckerTestContainers() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withLocale(Locale.UK).withZone(ZoneId.systemDefault());
    }

    @BeforeEach
    public void setUp() throws ExecutionException, InterruptedException {
        createConsumer();
        createProducer();
    }

    @AfterEach
    public void tearDown() {
        producer = null;
        consumer.stop();
        consumer = null;
    }

    @Test
    public void shouldNotDetectFraudCases() throws InterruptedException, ExecutionException {
        Instant instant = Instant.now();
        List<Movement> movements = Arrays.asList(
                Movement.builder().id("m0").amount(10f).device("atm-1").site("site0").origin(ATM_MOVEMENT).card("c1").createdAt(formatter.format(instant)).build(),
                Movement.builder().id("m1").amount(10f).device("atm-1").site("site1").origin(ATM_MOVEMENT).card("c1").createdAt(formatter.format(instant)).build(),
                Movement.builder().id("m2").amount(10f).device("atm-1").site("site2").origin(MERCHANT_MOVEMENT).card("c1").createdAt(formatter.format(instant)).build(),
                Movement.builder().id("m3").amount(10f).device("atm-1").site("site1").origin(ONLINE_MOVEMENT).card("c1").createdAt(formatter.format(instant)).build(),
                Movement.builder().id("m4").amount(10f).device("atm-2").site("site3").origin(ATM_MOVEMENT).card("c2").createdAt(formatter.format(instant)).build(),
                Movement.builder().id("m5").amount(10f).device("atm-1").site("site3").origin(MERCHANT_MOVEMENT).card("c2").createdAt(formatter.format(instant)).build(),
                Movement.builder().id("m6").amount(10f).device("atm-1").site("site0").origin(ATM_MOVEMENT).card("c3").createdAt(formatter.format(instant)).build()
        );

        int[] waitingIntervals = {0, 15, 46, 10, 5, 61, 5};
        for (int i=0; i<waitingIntervals.length; i++) {
            Thread.sleep(waitingIntervals[i] * 1000);
            Movement movement = movements.get(i);
            movement.setCreatedAt(formatter.format(Instant.now()));
            producer.send(new ProducerRecord<>(fraudCheckerConfig.getMovementsTopic(), movement.getCard(), movement)).get();
            log.info("Published movement: " + movement);
        }

        startOutputTopicConsumer();
        FraudCase fraudCase = output.poll(60, TimeUnit.SECONDS);
        assertNull(fraudCase);
    }

    @Test
    public void shouldDetectTwoPhysicalFraudCasesBecauseOperationsInMultipleDevicesDuringAShortPeriod() throws InterruptedException, ExecutionException {
        List<Movement> movements = Arrays.asList(
                Movement.builder().id("m0").card("c1").amount(10f).device("atm-1").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m1").card("c1").amount(10f).device("atm-2").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m2").card("c1").amount(10f).device("atm-1").origin(MERCHANT_MOVEMENT).build(),
                Movement.builder().id("m3").card("c1").amount(10f).device("atm-1").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m4").card("c2").amount(10f).device("atm-2").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m5").card("c2").amount(10f).device("atm-1").origin(MERCHANT_MOVEMENT).build(),
                Movement.builder().id("m6").card("c3").amount(10f).device("atm-1").origin(ATM_MOVEMENT).build()
        );

        int[] waitingIntervals = {0, 15, 20, 10, 5, 10, 65};
        for (int i=0; i<waitingIntervals.length; i++) {
            Thread.sleep(waitingIntervals[i] * 1000);
            Movement movement = movements.get(i);
            movement.setCreatedAt(formatter.format(Instant.now()));
            producer.send(new ProducerRecord<>(fraudCheckerConfig.getMovementsTopic(), movement.getCard(), movement)).get();
            log.info("Published movement: " + movement);
        }

        startOutputTopicConsumer();
        FraudCase fraudCaseC1 = output.poll(60, TimeUnit.SECONDS);
        FraudCase fraudCaseC2 = output.poll(5, TimeUnit.SECONDS);
        assertNotNull(fraudCaseC1);
        assertEquals("c1", fraudCaseC1.getCard());
        assertNotNull(fraudCaseC2);
        assertEquals("c2", fraudCaseC2.getCard());
    }

    @Test
    public void shouldDetectOnePhysicalFraudCaseBecauseMoreThanFourMovementsInAShortPeriod() throws InterruptedException, ExecutionException {
        startOutputTopicConsumer();

        List<Movement> movements = Arrays.asList(
                Movement.builder().id("m0").card("c1").amount(10f).device("atm-1").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m1").card("c1").amount(10f).device("atm-2").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m2").card("c1").amount(10f).device("atm-1").origin(MERCHANT_MOVEMENT).build(),
                Movement.builder().id("m3").card("c1").amount(10f).device("atm-1").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m4").card("c1").amount(10f).device("atm-2").origin(ATM_MOVEMENT).build(),
                Movement.builder().id("m5").card("c2").amount(10f).device("atm-2").origin(ATM_MOVEMENT).build()
        );

        int[] waitingIntervals = {0, 5, 10, 5, 5, 65};
        for (int i=0; i<waitingIntervals.length; i++) {
            Thread.sleep(waitingIntervals[i] * 1000);
            Movement movement = movements.get(i);
            movement.setCreatedAt(formatter.format(Instant.now()));
            producer.send(new ProducerRecord<>(fraudCheckerConfig.getMovementsTopic(), movement.getCard(), movement)).get();
            log.info("Published movement: " + movement);
        }

        FraudCase fraudCaseC1 = output.poll(60, TimeUnit.SECONDS);
        assertNotNull(fraudCaseC1);
        assertEquals("c1", fraudCaseC1.getCard());
    }

    @Test
    public void shouldDetectOnlineFraudCaseBecauseMultipleMovements() throws InterruptedException, ExecutionException {
        startOutputTopicConsumer();

        List<Movement> movements = Arrays.asList(
                Movement.builder().id("m0").card("c1").amount(10f).site("site-1").origin(ONLINE_MOVEMENT).build(),
                Movement.builder().id("m1").card("c1").amount(100f).site("site-2").origin(ONLINE_MOVEMENT).build(),
                Movement.builder().id("m2").card("c1").amount(50f).site("site-3").origin(ONLINE_MOVEMENT).build(),
                Movement.builder().id("m3").card("c1").amount(50f).site("site-4").origin(ONLINE_MOVEMENT).build(),
                Movement.builder().id("m4").card("c2").amount(10f).device("atm-1").origin(ONLINE_MOVEMENT).build()
        );

        int[] waitingIntervals = {0, 15, 5, 20, 65};
        for (int i=0; i<waitingIntervals.length; i++) {
            Thread.sleep(waitingIntervals[i] * 1000);
            Movement movement = movements.get(i);
            movement.setCreatedAt(formatter.format(Instant.now()));
            producer.send(new ProducerRecord<>(fraudCheckerConfig.getMovementsTopic(), movement.getCard(), movement)).get();
            log.info("Published movement: " + movement);
        }

        FraudCase fraudCaseC1 = output.poll(60, TimeUnit.SECONDS);
        assertNotNull(fraudCaseC1);
        assertEquals("c1", fraudCaseC1.getCard());
        assertEquals(210f, fraudCaseC1.getTotalAmount());
    }

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",KAFKA::getBootstrapServers);
    }

    private void createProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        producer = new KafkaProducer(props);
    }

    private void createConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "check-results");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // set up the consumer for the word count output
        DefaultKafkaConsumerFactory<String, FraudCase> cf = new DefaultKafkaConsumerFactory<>(props);
        ContainerProperties containerProperties = new ContainerProperties(fraudCheckerConfig.getFraudTopic());
        consumer = new KafkaMessageListenerContainer<>(cf, containerProperties);
        consumer.setBeanName("templateTests");

        consumer.setupMessageListener((MessageListener<String, FraudCase>) record -> {
            log.info("Record received: {}", record);
            output.add(record.value());
        });
    }

    private void startOutputTopicConsumer() {
        consumer.start();
    }


}