package com.tenagrim.meshgrouptestassg.controller;


import com.tenagrim.meshgrouptestassg.dto.request.LoginRequest;
import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.security.JwtService;
import com.tenagrim.meshgrouptestassg.service.UserLookupService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserLookupService userLookupService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserLookupService userLookupService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userLookupService = userLookupService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        User user = userLookupService.findByLogin(request.getLogin());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getName(), request.getPassword())
        );

        return jwtService.generateToken( user.getId());
    }
}
