package com.tenagrim.meshgrouptestassg.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class UserSearchRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    private String phone;

    private String name;

    private String email;

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String cacheKey(int page, int size) {
        return String.join("|",
                String.valueOf(dateOfBirth),
                normalize(phone),
                normalize(email),
                String.valueOf(page),
                String.valueOf(size),
                normalize(name));
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}
