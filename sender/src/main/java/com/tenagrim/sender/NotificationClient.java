package com.tenagrim.sender;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Component
public class NotificationClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final RetryBackoffSpec retry;

    public NotificationClient(WebClient notificationWebClient, CircuitBreaker notificationCircuitBreaker,
                              SenderProperties properties) {
        this.webClient = notificationWebClient;
        this.circuitBreaker = notificationCircuitBreaker;
        SenderProperties.Retry settings = properties.notification().retry();
        this.retry = Retry.backoff(settings.maxAttempts() - 1, settings.minBackoff())
                .maxBackoff(settings.maxBackoff())
                .jitter(0.5)
                .filter(e -> !(e instanceof CallNotPermittedException) && !isPermanent(e))
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    public Mono<Void> send(String payload) {
        return webClient.post()
                .uri("/notify")
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .then()
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .retryWhen(retry);
    }

    public static boolean isPermanent(Throwable e) {
        if (!(e instanceof WebClientResponseException response)) {
            return false;
        }
        int status = response.getStatusCode().value();
        return response.getStatusCode().is4xxClientError()
                && status != HttpStatus.REQUEST_TIMEOUT.value()
                && status != HttpStatus.TOO_MANY_REQUESTS.value();
    }
}
