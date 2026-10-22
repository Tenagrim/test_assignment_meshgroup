package com.tenagrim.meshgrouptestassg.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class EmailUpdateRequest {

    @NotBlank
    @Email
    @Size(max = 200)
    private String oldEmail;

    @NotBlank
    @Email
    @Size(max = 200)
    private String newEmail;

    public String getOldEmail() { return oldEmail; }
    public void setOldEmail(String oldEmail) { this.oldEmail = oldEmail; }

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
}
