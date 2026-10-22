package com.tenagrim.sender;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
public class NotificationListener {

    private final WebClient webClient;

    public NotificationListener(WebClient webClient) {
        this.webClient = webClient;
    }

    @KafkaListener(id = "notificationListener", topics = "notification")
    public Mono<Void> listen(String data) {
        log.info("Received message: {}", data);
        return webClient.post()
                .uri("/notify")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
