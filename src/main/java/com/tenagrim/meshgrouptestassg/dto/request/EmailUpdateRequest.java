package com.tenagrim.meshgrouptestassg.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class EmailUpdateRequest {

    @NotBlank
    @Email
    private String oldEmail;

    @NotBlank
    @Email
    private String newEmail;

    public String getOldEmail() { return oldEmail; }
    public void setOldEmail(String oldEmail) { this.oldEmail = oldEmail; }

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
}
