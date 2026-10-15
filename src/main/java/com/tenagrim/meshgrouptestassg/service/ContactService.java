package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.dto.request.*;
import com.tenagrim.meshgrouptestassg.entity.EmailData;
import com.tenagrim.meshgrouptestassg.entity.PhoneData;
import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.exception.*;
import com.tenagrim.meshgrouptestassg.repository.EmailDataRepository;
import com.tenagrim.meshgrouptestassg.repository.PhoneDataRepository;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class ContactService {

    private final UserRepository userRepository;
    private final EmailDataRepository emailRepository;
    private final PhoneDataRepository phoneRepository;

    public ContactService(UserRepository userRepository,
                          EmailDataRepository emailRepository,
                          PhoneDataRepository phoneRepository) {
        this.userRepository = userRepository;
        this.emailRepository = emailRepository;
        this.phoneRepository = phoneRepository;
    }

    @Transactional
    public EmailData addEmail(Long userId, EmailRequest request) {
        if (emailRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyTakenException(request.getEmail());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        EmailData email = new EmailData();
        email.setUser(user);
        email.setEmail(request.getEmail());
        return emailRepository.save(email);
    }

    @Transactional
    public EmailData updateEmail(Long userId, EmailUpdateRequest request) {
        EmailData email = emailRepository.findByEmail(request.getOldEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Email not found: " + request.getOldEmail()));

        if (!email.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Email does not belong to current user");
        }

        if (request.getNewEmail().equalsIgnoreCase(request.getOldEmail())) {
            return email;
        }

        if (emailRepository.existsByEmail(request.getNewEmail())) {
            throw new EmailAlreadyTakenException(request.getNewEmail());
        }

        email.setEmail(request.getNewEmail());
        return emailRepository.save(email);
    }

    @Transactional
    public void deleteEmail(Long userId, EmailRequest request) {
        EmailData email = emailRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Email not found: " + request.getEmail()));

        if (!email.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Email does not belong to current user");
        }

        if (emailRepository.countByUserId(userId) <= 1) {
            throw new LastContactException("User must have at least one email");
        }

        emailRepository.delete(email);
    }


    @Transactional
    public PhoneData addPhone(Long userId, PhoneRequest request) {
        if (phoneRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyTakenException(request.getPhone());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PhoneData phone = new PhoneData();
        phone.setUser(user);
        phone.setPhone(request.getPhone());
        return phoneRepository.save(phone);
    }

    @Transactional
    public PhoneData updatePhone(Long userId, PhoneUpdateRequest request) {
        PhoneData phone = phoneRepository.findByPhone(request.getOldPhone())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Phone not found: " + request.getOldPhone()));

        if (!phone.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Phone does not belong to current user");
        }

        if (request.getNewPhone().equals(request.getOldPhone())) {
            return phone;
        }

        if (phoneRepository.existsByPhone(request.getNewPhone())) {
            throw new PhoneAlreadyTakenException(request.getNewPhone());
        }

        phone.setPhone(request.getNewPhone());
        return phoneRepository.save(phone);
    }

    @Transactional
    public void deletePhone(Long userId, PhoneRequest request) {
        PhoneData phone = phoneRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Phone not found: " + request.getPhone()));

        if (!phone.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Phone does not belong to current user");
        }

        if (phoneRepository.countByUserId(userId) <= 1) {
            throw new LastContactException("User must have at least one phone");
        }

        phoneRepository.delete(phone);
    }
}