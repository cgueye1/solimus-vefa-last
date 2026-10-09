package com.wakana.vefa.dto.response;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyRevenueResponseDTO {

    private Integer year;
    private double totalRevenue;
    private List<ChartValueDTO> monthlyRevenue;
}
