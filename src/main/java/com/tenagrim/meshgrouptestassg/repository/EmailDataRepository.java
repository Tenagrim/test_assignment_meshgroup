package com.tenagrim.meshgrouptestassg.repository;

import com.tenagrim.meshgrouptestassg.entity.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailDataRepository extends JpaRepository<EmailData, Long> {
    List<EmailData> findAllByUserId(Long userId);
}
