package com.tenagrim.meshgrouptestassg.controller;

import com.tenagrim.meshgrouptestassg.AbstractIntegrationTest;
import com.tenagrim.meshgrouptestassg.entity.Account;
import com.tenagrim.meshgrouptestassg.entity.EmailData;
import com.tenagrim.meshgrouptestassg.entity.PhoneData;
import com.tenagrim.meshgrouptestassg.entity.User;
import com.tenagrim.meshgrouptestassg.repository.UserRepository;
import com.tenagrim.meshgrouptestassg.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String INVALID_CREDENTIALS = "Invalid login or password";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void login_byEmail_returnsTokenForThatUser() throws Exception {
        String token = login("ivan.ivanov@example.com", "password123")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    void login_byPhone_returnsTokenForThatUser() throws Exception {
        String token = login("79123456789", "password456")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(jwtService.extractUserId(token)).isEqualTo(2L);
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        login("ivan.ivanov@example.com", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
    }

    @Test
    void login_unknownUser_returns401() throws Exception {
        login("nobody@example.com", "password123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
    }

    @Test
    void login_notEmailOrPhone_returns401() throws Exception {
        login("Иван Иванов", "password123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
    }

    @Test
    void login_blankFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.login").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void login_namesakes_eachGetsOwnTokenAndCannotUseOthersPassword() throws Exception {
        User first = userRepository.save(user("Однофамилец Тестовый", "namesake1@example.com", "79990000001", "namesake-one"));
        User second = userRepository.save(user("Однофамилец Тестовый", "namesake2@example.com", "79990000002", "namesake-two"));

        String firstToken = login("namesake1@example.com", "namesake-one")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String secondToken = login("79990000002", "namesake-two")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(jwtService.extractUserId(firstToken)).isEqualTo(first.getId());
        assertThat(jwtService.extractUserId(secondToken)).isEqualTo(second.getId());

        login("namesake2@example.com", "namesake-one")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));
    }

    private ResultActions login(String login, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"" + login + "\",\"password\":\"" + password + "\"}"));
    }

    private static User user(String name, String email, String phone, String password) {
        User user = new User();
        user.setName(name);
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setPassword(password);

        Account account = new Account();
        account.setUser(user);
        account.setBalance(new BigDecimal("100.00"));
        account.setInitialBalance(new BigDecimal("100.00"));
        user.setAccount(account);

        EmailData emailData = new EmailData();
        emailData.setUser(user);
        emailData.setEmail(email);
        user.getEmails().add(emailData);

        PhoneData phoneData = new PhoneData();
        phoneData.setUser(user);
        phoneData.setPhone(phone);
        user.getPhones().add(phoneData);

        return user;
    }
}
