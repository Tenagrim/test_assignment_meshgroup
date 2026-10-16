package com.tenagrim.meshgrouptestassg.controller;

import com.tenagrim.meshgrouptestassg.dto.request.UserSearchRequest;
import com.tenagrim.meshgrouptestassg.dto.response.PageResponse;
import com.tenagrim.meshgrouptestassg.dto.response.UserResponse;
import com.tenagrim.meshgrouptestassg.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


@Tag(name = "Users", description = "Операции с пользователями")
@RestController
@Validated
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @Operation(summary = "Поиск пользователей с фильтрацией и пагинацией")
    @GetMapping
    public PageResponse<UserResponse> search(
            @ModelAttribute UserSearchRequest filter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return userService.search(filter, page, size);
    }

    @Operation(summary = "Получить свой профиль с контактами")
    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userService.getById(userId);
    }
}