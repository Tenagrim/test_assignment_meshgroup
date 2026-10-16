package com.tenagrim.meshgrouptestassg.controller;

import com.tenagrim.meshgrouptestassg.AbstractIntegrationTest;
import com.tenagrim.meshgrouptestassg.cache.CacheNames;
import com.tenagrim.meshgrouptestassg.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserCacheIntegrationTest extends AbstractIntegrationTest {

    private static final long ANNA = 3L;
    private static final String ANNA_NAME = "Анна Сидорова";
    private static final String ANNA_EMAIL = "anna.sidorova@example.com";
    private static final String CHANGED_NAME = "Изменено Напрямую";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    @AfterEach
    void restoreName() {
        jdbcTemplate.update("UPDATE \"user\" SET name = ? WHERE id = ?", ANNA_NAME, ANNA);
    }

    @Test
    void getById_isServedFromRedisUntilEvicted() throws Exception {
        getUser(ANNA).andExpect(jsonPath("$.name").value(ANNA_NAME));

        assertThat(redisTemplate.hasKey(CacheNames.USERS + "::" + ANNA)).isTrue();
        assertThat(redisTemplate.getExpire(CacheNames.USERS + "::" + ANNA)).isPositive();

        renameDirectlyInDb();

        getUser(ANNA).andExpect(jsonPath("$.name").value(ANNA_NAME));
        me(ANNA).andExpect(jsonPath("$.name").value(ANNA_NAME));
    }

    @Test
    void contactChange_evictsUserEntry() throws Exception {
        getUser(ANNA).andExpect(jsonPath("$.emails", not(hasItem("anna.cache@example.com"))));

        send(post("/api/users/me/emails"), ANNA, "{\"email\":\"anna.cache@example.com\"}")
                .andExpect(status().isCreated());

        getUser(ANNA).andExpect(jsonPath("$.emails", hasItem("anna.cache@example.com")));

        send(post("/api/users/me/emails/delete"), ANNA, "{\"email\":\"anna.cache@example.com\"}")
                .andExpect(status().isNoContent());

        getUser(ANNA).andExpect(jsonPath("$.emails", not(hasItem("anna.cache@example.com"))));
    }

    @Test
    void search_isServedFromRedisAndEvictedOnContactChange() throws Exception {
        search(ANNA, "Анна").andExpect(jsonPath("$.content[0].name").value(ANNA_NAME));

        renameDirectlyInDb();

        search(ANNA, "Анна")
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value(ANNA_NAME));

        send(put("/api/users/me/phones"), ANNA, "{\"oldPhone\":\"79998887766\",\"newPhone\":\"79998887700\"}")
                .andExpect(status().isOk());

        search(ANNA, "Анна").andExpect(jsonPath("$.totalElements").value(0));

        send(put("/api/users/me/phones"), ANNA, "{\"oldPhone\":\"79998887700\",\"newPhone\":\"79998887766\"}")
                .andExpect(status().isOk());
    }

    @Test
    void search_findsByNewEmailRightAfterChange() throws Exception {
        searchByEmail(ANNA, "anna.new@example.com").andExpect(jsonPath("$.totalElements").value(0));

        send(put("/api/users/me/emails"), ANNA, "{\"oldEmail\":\"" + ANNA_EMAIL + "\",\"newEmail\":\"anna.new@example.com\"}")
                .andExpect(status().isOk());

        searchByEmail(ANNA, "anna.new@example.com").andExpect(jsonPath("$.content[0].id").value(ANNA));
        searchByEmail(ANNA, ANNA_EMAIL).andExpect(jsonPath("$.totalElements").value(0));

        send(put("/api/users/me/emails"), ANNA, "{\"oldEmail\":\"anna.new@example.com\",\"newEmail\":\"" + ANNA_EMAIL + "\"}")
                .andExpect(status().isOk());
    }

    private void renameDirectlyInDb() {
        jdbcTemplate.update("UPDATE \"user\" SET name = ? WHERE id = ?", CHANGED_NAME, ANNA);
    }

    private ResultActions getUser(long id) throws Exception {
        return mockMvc.perform(get("/api/users/" + id)
                        .header("Authorization", "Bearer " + jwtService.generateToken(id)))
                .andExpect(status().isOk());
    }

    private ResultActions me(long userId) throws Exception {
        return mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + jwtService.generateToken(userId)))
                .andExpect(status().isOk());
    }

    private ResultActions search(long userId, String name) throws Exception {
        return mockMvc.perform(get("/api/users")
                        .param("name", name)
                        .param("phone", "79998887766")
                        .header("Authorization", "Bearer " + jwtService.generateToken(userId)))
                .andExpect(status().isOk());
    }

    private ResultActions searchByEmail(long userId, String email) throws Exception {
        return mockMvc.perform(get("/api/users")
                        .param("email", email)
                        .header("Authorization", "Bearer " + jwtService.generateToken(userId)))
                .andExpect(status().isOk());
    }

    private ResultActions send(MockHttpServletRequestBuilder builder, long userId, String body) throws Exception {
        return mockMvc.perform(builder
                .header("Authorization", "Bearer " + jwtService.generateToken(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
