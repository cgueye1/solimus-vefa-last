package com.wakana.vefa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionPlanPercentageDTO {
    private String planName;
    private double percentage;
}
