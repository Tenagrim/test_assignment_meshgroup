package com.tenagrim.meshgrouptestassg.controller;


import com.tenagrim.meshgrouptestassg.dto.request.LoginRequest;
import com.tenagrim.meshgrouptestassg.security.JwtService;
import com.tenagrim.meshgrouptestassg.util.Masking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed: login={}", Masking.login(request.getLogin()));
            throw e;
        }

        Long userId = Long.valueOf(authentication.getName());
        log.info("Login succeeded: userId={} login={}", userId, Masking.login(request.getLogin()));
        return jwtService.generateToken(userId);
    }
}
