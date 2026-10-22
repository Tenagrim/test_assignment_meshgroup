package com.tenagrim.meshgrouptestassg.controller;

import com.tenagrim.meshgrouptestassg.dto.request.ChangeBalanceRequest;
import com.tenagrim.meshgrouptestassg.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "Accounts", description = "Управление счетом. Снятие/зачисление")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Снять средства со счета")
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(Authentication auth,
                                                     @Valid @RequestBody ChangeBalanceRequest request) {
        Long userId = (Long) auth.getPrincipal();
        accountService.changeBalance(userId, request.getValue().negate());
        return ResponseEntity.ok("ok");
    }
    @Operation(summary = "Зачислить средства на счет")
    @PostMapping("/credit")
    public ResponseEntity<String> credit(Authentication auth,
                                           @Valid @RequestBody ChangeBalanceRequest request) {
        Long userId = (Long) auth.getPrincipal();
        accountService.changeBalance(userId, request.getValue());
        return ResponseEntity.ok("ok");
    }
}
