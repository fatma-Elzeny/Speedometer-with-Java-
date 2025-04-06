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
    /***
     *  SerialPort: SerialPort - Manages the UART connection.
     */
    private SerialPort serialPort;
    /***
     * running: boolean (volatile) - Controls the reading thread
     */
    private volatile boolean running = true;
    /***
     * LatestData: GPSData - Stores the most recent parsed GPS data.
     */
    private GPSData latestData = GPSData.noFix();
    
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
                            latestData = processDATA(line);
                        }
                    }
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error reading UART: " + e.getMessage());
        }
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