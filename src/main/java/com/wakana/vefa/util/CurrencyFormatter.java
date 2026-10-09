package com.wakana.vefa.util;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    
    public static String formatCFA(double amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRENCH);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        format.setGroupingUsed(true);
        return format.format(amount) + " F CFA";
    }
    
    // For Thymeleaf compatibility
    public String format(double amount) {
        return formatCFA(amount);
    }
}