package com.tenagrim.meshgrouptestassg.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmailRequest {

    @NotBlank
    @Email
    @Size(max = 200)
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
