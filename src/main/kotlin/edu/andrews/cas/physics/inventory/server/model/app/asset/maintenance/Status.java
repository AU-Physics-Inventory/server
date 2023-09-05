package edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance;

public enum Status {
    WORKING("W"),
    CALIBRATION("C"),
    REPAIR("R"),
    TESTING("T"),
    UNKNOWN("U");

    private final String code;

    Status(String code) {
        this.code = code;
    }

    public static Status lookup(String s) {
        return switch (s) {
            case "W", "Working" -> WORKING;
            case "C", "Out for calibration" -> CALIBRATION;
            case "R", "Out for repair" -> REPAIR;
            case "T", "Out for testing" -> TESTING;
            default -> UNKNOWN;
        };
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}
