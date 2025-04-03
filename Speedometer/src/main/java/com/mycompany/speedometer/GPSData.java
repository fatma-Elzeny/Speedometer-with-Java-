/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.speedometer;

/**
 *
 * @author awadin
 */

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

    // Static method for invalid data
    public static GPSData noFix() {
        return new GPSData(0.0, 0.0, 0.0, "", "", false);
    }
}
