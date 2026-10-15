package com.tenagrim.meshgrouptestassg.service;

import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserLookupService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{11}$"); // 79207865432

    private final UserRepository userRepository;

    public UserLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByLogin(String login) {
        if (EMAIL_PATTERN.matcher(login).matches()) {
            return userRepository.findByEmail(login)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found by email: " + login));
        }
        if (PHONE_PATTERN.matcher(login).matches()) {
            return userRepository.findByPhone(login)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found by phone: " + login));
        }
        throw new UsernameNotFoundException("Login must be email or phone: " + login);
    }
}
