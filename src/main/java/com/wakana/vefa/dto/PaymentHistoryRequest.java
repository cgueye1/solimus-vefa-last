package com.wakana.vefa.dto;

import lombok.Data;

@Data
public class PaymentHistoryRequest {

    private Long userId;
    private Long planId;
    private double amount;
    private int installmentCount;
    private String number;
}
