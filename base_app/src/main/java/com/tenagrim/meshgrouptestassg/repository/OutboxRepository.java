package com.tenagrim.meshgrouptestassg.repository;

import com.tenagrim.meshgrouptestassg.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    @Modifying
    @Query(value = "DELETE FROM outbox_notification WHERE created_at < :cutoff", nativeQuery = true)
    int deleteByCreatedAtBefore(@Param("cutoff") OffsetDateTime cutoff);
}
