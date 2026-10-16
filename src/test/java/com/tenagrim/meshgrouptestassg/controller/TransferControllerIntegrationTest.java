package com.tenagrim.meshgrouptestassg.controller;

import com.tenagrim.meshgrouptestassg.AbstractIntegrationTest;
import com.tenagrim.meshgrouptestassg.dto.request.TransferRequest;
import com.tenagrim.meshgrouptestassg.exception.InsufficientFundsException;
import com.tenagrim.meshgrouptestassg.security.JwtService;
import com.tenagrim.meshgrouptestassg.service.AccountService;
import com.tenagrim.meshgrouptestassg.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerIntegrationTest extends AbstractIntegrationTest {

    private static final long IVAN = 1L;
    private static final long PETR = 2L;
    private static final long ANNA = 3L;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetBalances() {
        accountService.resetBalancesAll();
    }

    @Test
    void transfer_withTokenFromLogin_movesMoney() throws Exception {
        String token = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"ivan.ivanov@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(PETR, "100.25")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromUserId").value(IVAN))
                .andExpect(jsonPath("$.toUserId").value(PETR))
                .andExpect(jsonPath("$.amount").value(100.25))
                .andExpect(jsonPath("$.fromBalanceAfter").value(900.25))
                .andExpect(jsonPath("$.toBalanceAfter").value(25100.25));

        assertThat(balance(IVAN)).isEqualByComparingTo("900.25");
        assertThat(balance(PETR)).isEqualByComparingTo("25100.25");
    }

    @Test
    void transfer_insufficientFunds_returns422AndKeepsBalances() throws Exception {
        transfer(ANNA, body(IVAN, "500.76"))
                .andExpect(status().isUnprocessableEntity());

        assertThat(balance(ANNA)).isEqualByComparingTo("500.75");
        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
    }

    @Test
    void transfer_negativeAmount_returns400AndKeepsBalances() throws Exception {
        transfer(IVAN, body(PETR, "-100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.value").exists());

        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
        assertThat(balance(PETR)).isEqualByComparingTo("25000.00");
    }

    @Test
    void transfer_moreThanTwoDecimals_returns400() throws Exception {
        transfer(IVAN, body(PETR, "10.005"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.value").exists());

        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
    }

    @Test
    void transfer_missingFields_returns400() throws Exception {
        transfer(IVAN, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.userId").exists())
                .andExpect(jsonPath("$.errors.value").exists());
    }

    @Test
    void transfer_toSelf_returns400() throws Exception {
        transfer(IVAN, body(IVAN, "10.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot transfer money to yourself"));

        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
    }

    @Test
    void transfer_unknownRecipient_returns404() throws Exception {
        transfer(IVAN, body(999L, "10.00"))
                .andExpect(status().isNotFound());

        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
    }

    @Test
    void transfer_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(PETR, "10.00")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Missing or malformed Authorization header"));

        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
    }

    @Test
    void transfer_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(PETR, "10.00")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired token"));

        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
    }

    @Test
    void concurrentOppositeTransfers_keepBalancesConsistent() throws Exception {
        int perDirection = 50;
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < perDirection; i++) {
            tasks.add(() -> transferService.transfer(IVAN, request(PETR, "10.00")));
            tasks.add(() -> transferService.transfer(PETR, request(IVAN, "10.00")));
        }

        List<Throwable> errors = runConcurrently(tasks);

        assertThat(errors).isEmpty();
        assertThat(balance(IVAN)).isEqualByComparingTo("1000.50");
        assertThat(balance(PETR)).isEqualByComparingTo("25000.00");
    }

    @Test
    void concurrentTransfers_neverOverdraw() throws Exception {
        int attempts = 20;
        AtomicInteger succeeded = new AtomicInteger();
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            tasks.add(() -> {
                transferService.transfer(ANNA, request(IVAN, "100.00"));
                return succeeded.incrementAndGet();
            });
        }

        List<Throwable> errors = runConcurrently(tasks);

        assertThat(succeeded.get()).isEqualTo(5);
        assertThat(errors).hasSize(attempts - 5).allMatch(e -> e instanceof InsufficientFundsException);
        assertThat(balance(ANNA)).isEqualByComparingTo("0.75");
        assertThat(balance(IVAN)).isEqualByComparingTo("1500.50");
    }

    private ResultActions transfer(long fromUserId, String body) throws Exception {
        return mockMvc.perform(post("/api/transfers")
                .header("Authorization", "Bearer " + jwtService.generateToken(fromUserId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private List<Throwable> runConcurrently(List<Callable<Object>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<Object> task : tasks) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();

            List<Throwable> errors = new ArrayList<>();
            for (Future<Object> future : futures) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    errors.add(e.getCause());
                }
            }
            return errors;
        } finally {
            executor.shutdownNow();
        }
    }

    private BigDecimal balance(long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE user_id = ?", BigDecimal.class, userId);
    }

    private static String body(long toUserId, String amount) {
        return "{\"userId\":" + toUserId + ",\"value\":" + amount + "}";
    }

    private static TransferRequest request(long toUserId, String amount) {
        TransferRequest request = new TransferRequest();
        request.setUserId(toUserId);
        request.setValue(new BigDecimal(amount));
        return request;
    }
}
