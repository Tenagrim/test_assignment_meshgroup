package com.tenagrim.sender;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationClient notificationClient;
    private final int concurrency;

    public NotificationListener(NotificationClient notificationClient, SenderProperties properties) {
        this.notificationClient = notificationClient;
        this.concurrency = properties.notification().concurrency();
    }

    @KafkaListener(id = "notificationListener", idIsGroup = false, topics = "${app.kafka.topic}", batch = "true")
    public void listen(List<ConsumerRecord<String, String>> records) {
        List<Optional<Throwable>> results = Flux.fromIterable(records)
                .flatMapSequential(this::deliver, concurrency)
                .collectList()
                .block();

        for (int i = 0; i < results.size(); i++) {
            Optional<Throwable> error = results.get(i);
            if (error.isEmpty()) {
                continue;
            }
            ConsumerRecord<String, String> record = records.get(i);
            if (NotificationClient.isPermanent(error.get())) {
                log.error("Notification rejected, skipped: key={} partition={} offset={} reason={}",
                        record.key(), record.partition(), record.offset(), error.get().getMessage());
                continue;
            }
            log.warn("Notification delivery failed: key={} partition={} offset={} batchSize={} reason={}",
                    record.key(), record.partition(), record.offset(), records.size(), error.get().toString());
            throw new BatchListenerFailedException("Notification delivery failed", error.get(), i);
        }
        log.debug("Notifications delivered: count={}", records.size());
    }

    private Mono<Optional<Throwable>> deliver(ConsumerRecord<String, String> record) {
        if (record.value() == null) {
            return Mono.just(Optional.empty());
        }
        return notificationClient.send(record.value())
                .then(Mono.just(Optional.<Throwable>empty()))
                .onErrorResume(e -> Mono.just(Optional.of(e)));
    }
}
