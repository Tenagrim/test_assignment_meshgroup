package com.tenagrim.meshgrouptestassg.dto.response;

import com.tenagrim.meshgrouptestassg.entity.EmailData;

public class EmailResponse {
    private String email;

    public static EmailResponse from(EmailData data) {
        EmailResponse r = new EmailResponse();
        r.email = data.getEmail();
        return r;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}