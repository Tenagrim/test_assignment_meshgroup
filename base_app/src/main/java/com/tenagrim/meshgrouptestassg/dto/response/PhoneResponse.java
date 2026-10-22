package com.tenagrim.meshgrouptestassg.dto.response;

import com.tenagrim.meshgrouptestassg.entity.PhoneData;

public class PhoneResponse {
    private String phone;

    public static PhoneResponse from(PhoneData data) {
        PhoneResponse r = new PhoneResponse();
        r.phone = data.getPhone();
        return r;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
