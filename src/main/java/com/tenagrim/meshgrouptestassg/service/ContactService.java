package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.cache.EvictUserCaches;
import com.tenagrim.meshgrouptestassg.dto.request.*;
import com.tenagrim.meshgrouptestassg.entity.EmailData;
import com.tenagrim.meshgrouptestassg.entity.PhoneData;
import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.exception.*;
import com.tenagrim.meshgrouptestassg.repository.EmailDataRepository;
import com.tenagrim.meshgrouptestassg.repository.PhoneDataRepository;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import com.tenagrim.meshgrouptestassg.util.AfterCommit;
import com.tenagrim.meshgrouptestassg.util.Emails;
import com.tenagrim.meshgrouptestassg.util.Masking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final UserRepository userRepository;
    private final EmailDataRepository emailRepository;
    private final PhoneDataRepository phoneRepository;

    private static final String PHONE_CONSTRAINT = "uq_phone_data_phone";
    private static final String EMAIL_CONSTRAINT = "uq_email_data_email";

    public ContactService(UserRepository userRepository,
                          EmailDataRepository emailRepository,
                          PhoneDataRepository phoneRepository) {
        this.userRepository = userRepository;
        this.emailRepository = emailRepository;
        this.phoneRepository = phoneRepository;
    }

    @EvictUserCaches
    @Transactional
    public EmailData addEmail(Long userId, EmailRequest request) {
        String value = Emails.normalize(request.getEmail());
        if (emailRepository.existsByEmail(value)) {
            throw new EmailAlreadyTakenException(value);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        EmailData email = new EmailData();
        email.setUser(user);
        email.setEmail(value);
        EmailData saved = saveEmail(email);
        AfterCommit.run(() -> log.info("Email added: userId={} email={}", userId, Masking.email(value)));
        return saved;
    }

    @EvictUserCaches
    @Transactional
    public EmailData updateEmail(Long userId, EmailUpdateRequest request) {
        String oldEmail = Emails.normalize(request.getOldEmail());
        String newEmail = Emails.normalize(request.getNewEmail());

        EmailData email = emailRepository.findByEmail(oldEmail)
                .orElseThrow(() -> new AccessDeniedException("Email does not belong to current user"));

        if (!email.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Email does not belong to current user");
        }

        if (newEmail.equals(oldEmail)) {
            return email;
        }

        if (emailRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyTakenException(newEmail);
        }

        email.setEmail(newEmail);
        EmailData saved = saveEmail(email);
        AfterCommit.run(() -> log.info("Email changed: userId={} from={} to={}",
                userId, Masking.email(oldEmail), Masking.email(newEmail)));
        return saved;
    }

    @EvictUserCaches
    @Transactional
    public void deleteEmail(Long userId, EmailRequest request) {
        String value = Emails.normalize(request.getEmail());
        EmailData email = emailRepository.findByEmail(value)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Email not found: " + value));

        if (!email.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Email does not belong to current user");
        }

        if (emailRepository.countByUserId(userId) <= 1) {
            throw new LastContactException("User must have at least one email");
        }

        emailRepository.delete(email);
        AfterCommit.run(() -> log.info("Email deleted: userId={} email={}", userId, Masking.email(value)));
    }


    @EvictUserCaches
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
        PhoneData saved = savePhone(phone);
        AfterCommit.run(() -> log.info("Phone added: userId={} phone={}", userId, Masking.phone(request.getPhone())));
        return saved;
    }

    @EvictUserCaches
    @Transactional
    public PhoneData updatePhone(Long userId, PhoneUpdateRequest request) {
        PhoneData phone = phoneRepository.findByPhone(request.getOldPhone())
                .orElseThrow(() -> new AccessDeniedException("Phone does not belong to current user"));

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
        PhoneData saved = savePhone(phone);
        AfterCommit.run(() -> log.info("Phone changed: userId={} from={} to={}",
                userId, Masking.phone(request.getOldPhone()), Masking.phone(request.getNewPhone())));
        return saved;
    }

    @EvictUserCaches
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
        AfterCommit.run(() -> log.info("Phone deleted: userId={} phone={}", userId, Masking.phone(request.getPhone())));
    }

    private EmailData saveEmail(EmailData email) {
        try {
            return emailRepository.saveAndFlush(email);
        } catch (DataIntegrityViolationException e) {
            if (isConstraint(e, EMAIL_CONSTRAINT))
                throw new EmailAlreadyTakenException(email.getEmail());
            else throw e;
        }
    }

    private PhoneData savePhone(PhoneData phone) {
        try {
            return phoneRepository.saveAndFlush(phone);
        } catch (DataIntegrityViolationException e) {
            if (isConstraint(e, PHONE_CONSTRAINT))
                throw new PhoneAlreadyTakenException(phone.getPhone());
            else throw e;
        }
    }

    private static boolean isConstraint(DataIntegrityViolationException e, String constraintName) {
        return e.getCause() instanceof org.hibernate.exception.ConstraintViolationException
                && constraintName.equalsIgnoreCase(
                ((org.hibernate.exception.ConstraintViolationException) e.getCause()).getConstraintName());
    }
}