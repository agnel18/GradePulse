package com.gradepulse.model;

public enum AttendanceStatus {
    PRESENT("Present", "✓"),
    ABSENT("Absent", "✗"),
    LATE("Late", "⏰"),
    HALF_DAY("Half Day", "½");

    private final String displayName;
    private final String symbol;

    AttendanceStatus(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}
