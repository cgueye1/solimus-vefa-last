package com.wakana.vefa.dto;

import lombok.Data;

@Data
public class OTPRequest {
    private String phoneNumber;
    private String otp;
}
