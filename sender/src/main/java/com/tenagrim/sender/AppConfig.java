package com.tenagrim.sender;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@EnableConfigurationProperties(SenderProperties.class)
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public WebClient notificationWebClient(WebClient.Builder builder, SenderProperties properties) {
        SenderProperties.Notification notification = properties.notification();
        ConnectionProvider connectionProvider = ConnectionProvider.builder("notification")
                .maxConnections(notification.concurrency())
                .pendingAcquireMaxCount(notification.concurrency())
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) notification.connectTimeout().toMillis())
                .responseTimeout(notification.responseTimeout());

        return builder
                .baseUrl(notification.url())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public CircuitBreaker notificationCircuitBreaker(SenderProperties properties) {
        SenderProperties.CircuitBreaker settings = properties.notification().circuitBreaker();
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(settings.failureRateThreshold())
                .slidingWindowSize(settings.slidingWindowSize())
                .minimumNumberOfCalls(settings.minimumNumberOfCalls())
                .waitDurationInOpenState(settings.waitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(settings.permittedCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordException(e -> !NotificationClient.isPermanent(e))
                .build();
        CircuitBreaker circuitBreaker = CircuitBreaker.of("notification", config);
        circuitBreaker.getEventPublisher().onStateTransition(event ->
                log.warn("Notification circuit breaker: {}", event.getStateTransition()));
        return circuitBreaker;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<?, ?> kafkaTemplate, SenderProperties properties) {
        SenderProperties.ErrorBackoff settings = properties.kafka().errorBackoff();
        ExponentialBackOff backOff = new ExponentialBackOff(settings.initialInterval().toMillis(), settings.multiplier());
        backOff.setMaxInterval(settings.maxInterval().toMillis());
        backOff.setMaxElapsedTime(settings.maxElapsedTime().toMillis());
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), backOff);
    }
}
