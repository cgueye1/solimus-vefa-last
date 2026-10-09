package com.wakana.vefa.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscriptionKpiDto {
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long expiredSubscriptions;

    private long weeklySubscriptions;
    private long monthlySubscriptions;
    private long yearlySubscriptions;

    private double totalRevenue;
    private double weeklyRevenue;
    private double monthlyRevenue;
    private double yearlyRevenue;

    private double paidRevenue;
    private double unpaidRevenue;

    private double paidPercentage;
    private double unpaidPercentage;
}
