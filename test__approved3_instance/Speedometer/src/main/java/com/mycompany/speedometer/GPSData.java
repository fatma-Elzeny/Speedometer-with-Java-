package com.mycompany.speedometer;

public class GPSData {
    private final double speedKmh;
    private final double latitude;
    private final double longitude;
    private final String latDirection;
    private final String lonDirection;
    private final boolean isValid;

    public GPSData(double speedKmh, double latitude, double longitude, String latDirection, String lonDirection, boolean isValid) {
        this.speedKmh = speedKmh;
        this.latitude = latitude;
        this.longitude = longitude;
        this.latDirection = latDirection;
        this.lonDirection = lonDirection;
        this.isValid = isValid;
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLatDirection() {
        return latDirection;
    }

    public String getLonDirection() {
        return lonDirection;
    }

    public boolean isValid() {
        return isValid;
    }

    public static GPSData noFix() {
        return new GPSData(0.0, 0.0, 0.0, "N/A", "N/A", false);
    }
}