package com.wakana.vefa.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallForChargeKpiResponse {
    private double totalAmount;
    private double paidAmount;
    private double unpaidAmount;
}
