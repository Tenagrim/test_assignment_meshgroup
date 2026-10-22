package com.tenagrim.meshgrouptestassg.controller;

import com.tenagrim.meshgrouptestassg.dto.request.TransferRequest;
import com.tenagrim.meshgrouptestassg.dto.response.TransferResponse;
import com.tenagrim.meshgrouptestassg.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "Transfers", description = "Переводы между пользователями")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(summary = "Перевести деньги другому пользователю")
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(Authentication auth,
                                                     @Valid @RequestBody TransferRequest request) {
        Long fromUserId = (Long) auth.getPrincipal();
        TransferResponse response = transferService.transfer(fromUserId, request);
        return ResponseEntity.ok(response);
    }
}