/***
 * GPSReader.java : 
 * - Handles communication with the GPS device via UART.
 * - Parses $GPRMC NMEA sentences
 * - Updates the latest GPS data.
 */

package com.mycompany.speedometer;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;

public class GPSReader {
    private SerialPort serialPort;
    private volatile boolean running = true;
    private volatile boolean isConnected = true;
    private GPSData latestData = GPSData.noFix();

    public void setupUART() {
        serialPort = SerialPort.getCommPort("/dev/ttyAMA0");
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 5000, 0);
        if (serialPort.openPort()) {
            System.out.println("UART opened successfully on /dev/ttyAMA0");
            new Thread(this::readUART).start();
        } else {
            System.err.println("Failed to open UART on /dev/ttyAMA0");
            isConnected = false;
        }
    }

    void readUART() {
        while (running) {
            try (InputStream input = serialPort.getInputStream()) {
                byte[] buffer = new byte[1024];
                StringBuilder sentence = new StringBuilder();
                while (running) {
                    try {
                        int bytesRead = input.read(buffer);
                        if (bytesRead > 0) {
                            String data = new String(buffer, 0, bytesRead);
                            sentence.append(data);
                            int newlineIndex;
                            while ((newlineIndex = sentence.indexOf("\n")) != -1) {
                                String line = sentence.substring(0, newlineIndex).trim();
                                sentence.delete(0, newlineIndex + 1);
                                if (!line.isEmpty()) {
                                    System.out.println("Raw UART data: " + line);
                                    System.out.println("Processing: " + line);
                                    latestData = processDATA(line);
                                }
                            }
                            if (!isConnected) {
                                System.out.println("GPS reconnected!");
                            }
                            isConnected = true; // Data received
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading UART: " + e.getMessage());
                        if (isConnected) {
                            System.err.println("GPS disconnected!");
                        }
                        isConnected = false;
                        while (running && !testConnection()) {
                            System.err.println("Attempting to reconnect to GPS...");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                    try {
                        Thread.sleep(100); // Added try-catch for compilation
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to open UART input stream: " + e.getMessage());
                isConnected = false;
                if (running) {
                    try {
                        Thread.sleep(1000); // Wait before retrying stream
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private boolean testConnection() {
        try {
            byte[] testBuffer = new byte[1];
            int bytesRead = serialPort.getInputStream().read(testBuffer);
            if (bytesRead > 0) {
                System.out.println("GPS reconnected!");
                return true;
            }
        } catch (IOException e) {
            // Still disconnected
        }
        return false;
    }

    private GPSData processDATA(String data) {
        String[] parts = data.split(",");
        if (data.startsWith("$GPRMC") && parts.length >= 12) {
            boolean hasFix = "A".equals(parts[2]);
            if (hasFix && !parts[3].isEmpty() && !parts[4].isEmpty()
                    && !parts[5].isEmpty() && !parts[6].isEmpty()
                    && !parts[7].isEmpty()) {
                try {
                    double latitude = parseCoordinate(parts[3], parts[4]);
                    double longitude = parseCoordinate(parts[5], parts[6]);
                    String latDir = parts[4];
                    String lonDir = parts[6];
                    double speedKnots = Double.parseDouble(parts[7]);
                    double speedKmh = speedKnots * 1.852;
                    return new GPSData(speedKmh, latitude, longitude, latDir, lonDir, true);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid GPRMC data: " + data);
                }
            }
            return GPSData.noFix();
        } else {
            System.out.println("Skipping unsupported or incomplete NMEA frame: " + data);
            return latestData;
        }
    }

    public void stop() {
        running = false;
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("UART closed");
        }
    }

    public GPSData getLatestData() {
        return latestData;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private double parseCoordinate(String value, String direction) {
        try {
            int degreeLength = (direction.equals("N") || direction.equals("S")) ? 2 : 3;
            double degrees = Double.parseDouble(value.substring(0, degreeLength));
            double minutes = Double.parseDouble(value.substring(degreeLength)) / 60.0;
            double result = degrees + minutes;
            return (direction.equals("S") || direction.equals("W")) ? -result : result;
        } catch (Exception e) {
            System.out.println("Invalid coordinate: " + value + " " + direction);
            return 0.0;
        }
    }
}


