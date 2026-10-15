package com.tenagrim.meshgrouptestassg.repository;

import com.tenagrim.meshgrouptestassg.entity.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {
    List<EmailData> findAllByUserId(Long userId);
    Optional<EmailData> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByUserId(Long userId);

}
