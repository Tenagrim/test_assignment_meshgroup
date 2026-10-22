package com.tenagrim.sender;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record SenderProperties(Kafka kafka, Notification notification) {

    public record Kafka(String topic, ErrorBackoff errorBackoff) {
    }

    public record ErrorBackoff(Duration initialInterval, double multiplier, Duration maxInterval,
                               Duration maxElapsedTime) {
    }

    public record Notification(String url, Duration connectTimeout, Duration responseTimeout, int concurrency,
                               Retry retry, CircuitBreaker circuitBreaker) {
    }

    public record Retry(int maxAttempts, Duration minBackoff, Duration maxBackoff) {
    }

    public record CircuitBreaker(float failureRateThreshold, int slidingWindowSize, int minimumNumberOfCalls,
                                 Duration waitDurationInOpenState, int permittedCallsInHalfOpenState) {
    }
}
