package com.tenagrim.meshgrouptestassg.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.tenagrim.meshgrouptestassg.config.Constants.PHONE_REGEX;

public class PhoneUpdateRequest {

    @NotBlank
    @Pattern(regexp = PHONE_REGEX)
    private String oldPhone;

    @NotBlank
    @Pattern(regexp = PHONE_REGEX)
    private String newPhone;

    public String getOldPhone() { return oldPhone; }
    public void setOldPhone(String oldPhone) { this.oldPhone = oldPhone; }

    public String getNewPhone() { return newPhone; }
    public void setNewPhone(String newPhone) { this.newPhone = newPhone; }
}