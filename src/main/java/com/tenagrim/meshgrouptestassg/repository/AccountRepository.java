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

}
