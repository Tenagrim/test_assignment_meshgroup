package com.tenagrim.meshgrouptestassg.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

import static com.tenagrim.meshgrouptestassg.config.Constants.DATE_FORMAT;

public class UserResponse {

    private Long id;
    private String name;
    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDate dateOfBirth;
    private List<String> emails;
    private List<String> phones;

    public UserResponse() {}

    public UserResponse(Long id, String name, LocalDate dateOfBirth,
                        List<String> emails, List<String> phones) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.emails = emails;
        this.phones = phones;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }

    public List<String> getPhones() { return phones; }
    public void setPhones(List<String> phones) { this.phones = phones; }
}
