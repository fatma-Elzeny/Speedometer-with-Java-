/***
 * GPSData.java: A dual-purpose class. 
 * - It acts as an immutable data holder for GPS information (speed, latitude, longitude, etc.) 
 * - And statically manages the single GPSReader instance.
 * 
 */

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
    /***
     * Instance fields - Store specific GPS data points.
     */
    private final double speedKmh;
    private final double latitude;
    private final double longitude;
    private final String latDirection;
    private final String lonDirection;
    private final boolean isValid;

    /***
     * reader: GPSReader (static) :
     * Holds the single GPSReader instance shared across the app.
     */
    private static GPSReader reader;

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

    // Static methods to manage the GPSReader
    public static void setReader(GPSReader gpsReader) {
        reader = gpsReader;
    }

    public static GPSReader getReader() {
        return reader;
    }
}