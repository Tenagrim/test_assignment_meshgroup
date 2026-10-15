package com.tenagrim.meshgrouptestassg.controller;

import com.tenagrim.meshgrouptestassg.dto.request.*;
import com.tenagrim.meshgrouptestassg.dto.response.EmailResponse;
import com.tenagrim.meshgrouptestassg.dto.response.PhoneResponse;
import com.tenagrim.meshgrouptestassg.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Contacts", description = "Управление email и телефоном текущего пользователя")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users/me")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "Добавить email")
    @PostMapping("/emails")
    public ResponseEntity<EmailResponse> addEmail(Authentication auth,
                                                  @Valid @RequestBody EmailRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EmailResponse.from(contactService.addEmail(userId, request)));
    }

    @Operation(summary = "Изменить email")
    @PutMapping("/emails")
    public EmailResponse updateEmail(Authentication auth,
                                     @Valid @RequestBody EmailUpdateRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return EmailResponse.from(contactService.updateEmail(userId, request));
    }

    @Operation(summary = "Удалить email")
    @PostMapping("/emails/delete")
    public ResponseEntity<Void> deleteEmail(Authentication auth,
                                            @Valid @RequestBody EmailRequest request) {
        Long userId = (Long) auth.getPrincipal();
        contactService.deleteEmail(userId, request);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Добавить телефон")
    @PostMapping("/phones")
    public ResponseEntity<PhoneResponse> addPhone(Authentication auth,
                                                  @Valid @RequestBody PhoneRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PhoneResponse.from(contactService.addPhone(userId, request)));
    }

    @Operation(summary = "Изменить телефон")
    @PutMapping("/phones")
    public PhoneResponse updatePhone(Authentication auth,
                                     @Valid @RequestBody PhoneUpdateRequest request) {
        Long userId = (Long) auth.getPrincipal();
        return PhoneResponse.from(contactService.updatePhone(userId, request));
    }

    @Operation(summary = "Удалить телефон")
    @PostMapping("/phones/delete")
    public ResponseEntity<Void> deletePhone(Authentication auth,
                                            @Valid @RequestBody PhoneRequest request) {
        Long userId = (Long) auth.getPrincipal();
        contactService.deletePhone(userId, request);
        return ResponseEntity.noContent().build();
    }
}