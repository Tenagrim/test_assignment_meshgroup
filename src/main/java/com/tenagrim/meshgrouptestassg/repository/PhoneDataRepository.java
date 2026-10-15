package com.tenagrim.meshgrouptestassg.repository;

import com.tenagrim.meshgrouptestassg.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {
    List<PhoneData> findAllByUserId(Long userId);
}
