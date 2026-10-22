package com.tenagrim.meshgrouptestassg.repository;

import com.tenagrim.meshgrouptestassg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("SELECT u FROM User u JOIN u.emails e WHERE e.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN u.phones p WHERE p.phone = :phone")
    Optional<User> findByPhone(@Param("phone") String phone);
}
