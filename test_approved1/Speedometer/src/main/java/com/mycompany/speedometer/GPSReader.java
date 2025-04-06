package com.mycompany.speedometer;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;

public class GPSReader {
    private SerialPort serialPort;
    private volatile boolean running = true;
    private double latestSpeedKmh = 0.0;
    private double latestLat = 0.0;
    private double latestLon = 0.0;
    private String latestLatDir = "N/A";
    private String latestLonDir = "N/A";
    private boolean hasFix = false;

    public void setupUART() {
        serialPort = SerialPort.getCommPort("/dev/ttyS0");
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 5000, 0);
        if (serialPort.openPort()) {
            System.out.println("UART opened successfully on /dev/ttyS0");
            new Thread(this::readUART).start();
        } else {
            System.err.println("Failed to open UART on /dev/ttyS0");
        }
    }

    void readUART() {
        try (InputStream input = serialPort.getInputStream()) {
            byte[] buffer = new byte[1024];
            StringBuilder sentence = new StringBuilder();
            while (running) {
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
                            processNMEA(line);
                        }
                    }
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error reading UART: " + e.getMessage());
        } finally {
            if (serialPort.isOpen()) {
                serialPort.closePort();
                System.out.println("UART closed");
            }
        }
    }

    private void processNMEA(String data) {
        String[] parts = data.split(",");
        if (data.startsWith("$GPRMC") && parts.length >= 12) {
            hasFix = "A".equals(parts[2]);
            if (hasFix && !parts[3].isEmpty() && !parts[7].isEmpty()) {
                try {
                    latestLat = parseCoordinate(parts[3], parts[4]);
                    latestLon = parseCoordinate(parts[5], parts[6]);
                    latestLatDir = parts[4];
                    latestLonDir = parts[6];
                    double speedKnots = Double.parseDouble(parts[7]);
                    latestSpeedKmh = speedKnots * 1.852;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid GPRMC data: " + data);
                }
            }
        } else if (data.startsWith("$GPGGA") && parts.length >= 10) {
            try {
                int fixQuality = Integer.parseInt(parts[6]);
                if (fixQuality > 0 && !parts[2].isEmpty() && !parts[4].isEmpty()) {
                    latestLat = parseCoordinate(parts[2], parts[3]);
                    latestLon = parseCoordinate(parts[4], parts[5]);
                    latestLatDir = parts[3];
                    latestLonDir = parts[5];
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid GPGGA data: " + data);
            }
        } else if (data.startsWith("$GPVTG") && parts.length >= 10) {
            try {
                if (hasFix && !parts[7].isEmpty()) {
                    double speedKnots = Double.parseDouble(parts[7]);
                    latestSpeedKmh = speedKnots * 1.852;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid GPVTG speed: " + data);
            }
        }
    }

    public double getSpeed() {
        return latestSpeedKmh;
    }
    public double getLatitude() {
        return latestLat;
    }
    public double getLongitude() {
        return latestLon;
    }
    public String getLatitudeDir() {
        return latestLatDir;
    }
    public String getLongitudeDir() {
        return latestLonDir;
    }
    public boolean hasFix() {
        return hasFix;
    }

    public void stopReading() {
        running = false;
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