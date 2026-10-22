package com.tenagrim.meshgrouptestassg.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.tenagrim.meshgrouptestassg.config.Constants.PHONE_REGEX;

public class PhoneRequest {

    @NotBlank
    @Pattern(regexp = PHONE_REGEX, message = "Phone must be 11 digits, e.g. 79207865432")
    private String phone;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
