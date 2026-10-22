package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.entity.Outbox;
import com.tenagrim.meshgrouptestassg.repository.OutboxRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxRepository repository;

    public OutboxService(OutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    public Outbox saveNotification(String payload) {
        Outbox outbox = new Outbox();
        outbox.setAggregatetype("notification");
        outbox.setAggregateid(UUID.randomUUID().toString());
        outbox.setType("NotificationCreated");
        outbox.setPayload(payload);

        return repository.save(outbox);
    }
}
