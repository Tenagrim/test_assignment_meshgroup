package com.tenagrim.meshgrouptestassg.service.scheduler;

import com.tenagrim.meshgrouptestassg.repository.OutboxRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;

@Component
@ConditionalOnProperty(prefix = "app.scheduler", name = "cleanup-outbox-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxCleanupScheduler {

    private final OutboxRepository repository;

    public OutboxCleanupScheduler(OutboxRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupOldOutboxEntries() {
        // каждый час удаляем записи больше часа.
        // для большой нагрузки это решение не подходит, создает высокую фрагментированность,
        // нужно настроить нормальное партиционирование и удалять партиции
        repository.deleteByCreatedAtBefore(OffsetDateTime.now().minusHours(1));
    }
}
