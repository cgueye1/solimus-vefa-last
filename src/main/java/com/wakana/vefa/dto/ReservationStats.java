package com.wakana.vefa.dto;

public class ReservationStats {
    private String month;
    private int value;

    public ReservationStats(int month, int value) {
        this.month = getMonthName(month);
        this.value = value;
    }

    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Janvier";
            case 2 -> "Février";
            case 3 -> "Mars";
            case 4 -> "Avril";
            case 5 -> "Mai";
            case 6 -> "Juin";
            case 7 -> "Juillet";
            case 8 -> "Août";
            case 9 -> "Septembre";
            case 10 -> "Octobre";
            case 11 -> "Novembre";
            case 12 -> "Décembre";
            default -> "Inconnu";
        };
    }

    public String getMonth() { return month; }
    public int getValue() { return value; }
}
