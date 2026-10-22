package com.tenagrim.meshgrouptestassg.repository;

import com.tenagrim.meshgrouptestassg.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    Optional<Account> findByUserIdForUpdate(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value =
        "UPDATE account " +
        "SET balance = LEAST(balance * CAST(:growthRate AS numeric), initial_balance * CAST(:maxMultiplier AS numeric)) " +
        "WHERE balance < initial_balance * CAST(:maxMultiplier AS numeric)"
        , nativeQuery = true)
    int applyInterestForAll(@Param("growthRate") BigDecimal growthRate,
                       @Param("maxMultiplier") BigDecimal maxMultiplier);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE account SET balance = initial_balance",
            nativeQuery = true)
    int initInitialBalance();

    @Modifying(clearAutomatically = true)
    @Query(value =
    "WITH updated AS (" +
    "    UPDATE account SET balance = balance + CAST(:amount AS numeric) WHERE user_id = :userId RETURNING id)" +
    "INSERT INTO outbox_notification (id, aggregatetype, aggregateid, type, payload, created_at)" +
    "SELECT gen_random_uuid(), 'notification', CAST(:userId AS text), 'BalanceChanged', CAST(:payload AS jsonb), now()" +
    "FROM updated"
    , nativeQuery = true)
    int changeBalanceAndSaveOutbox(@Param("userId") Long userId, @Param("amount") BigDecimal amount,@Param("payload") String payload);

}
