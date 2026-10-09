package com.wakana.vefa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyStatsDTO {
    private String month;
    private long total;
}
