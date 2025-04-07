/***
 * GPSReader.java : 
 * - Handles communication with the GPS device via UART.
 * - Parses $GPRMC NMEA sentences
 * - Updates the latest GPS data.
 */
/**
 * Where: GPSReader.setupUART() and GPSReader.readUART()
 * What Happens: App starts the GPSReader, 
 *    which opens the UART port 
 *      and begins reading GPS data in a background thread.
 */
package com.mycompany.speedometer;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author awadin
 */
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
        /**
         * Initializes the serialPort to connect to /dev/ttyS0 (the GPS device).
         */
        serialPort = SerialPort.getCommPort("/dev/ttyS0");
        /**
         * Configures UART settings 
         * 1- 9600 baud.
         * 2- 8 data bits.
         * 3- 1 stop bit.
         * 4- no parity.
         * 5- semi-blocking read with 5s timeout.
         */
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 5000, 0);
        /**
         * Attempts to open the UART port; returns true if successful
         */
        if (serialPort.openPort()) {
            System.out.println("UART opened successfully on /dev/ttyS0");
            /**
             * Starts a new thread running readUART() to continuously read GPS data.
             */
            new Thread(this::readUART).start();
        } else {
            System.err.println("Failed to open UART on /dev/ttyS0");
        }
    }

    void readUART() {
        /**
         * Opens an input stream from the UART port to read data; auto-closes when done.
         */
        try (InputStream input = serialPort.getInputStream()) {
            
            /**
             * Creates a 1024-byte buffer to hold incoming data.
             */
            byte[] buffer = new byte[1024];
            /**
             * Builds complete NMEA sentences from chunks of data.
             */
            StringBuilder sentence = new StringBuilder();
            /**
             * Loops as long as running is true (stops when stop() is called).
             */
            while (running) {
                /**
                 * Reads up to 1024 bytes from the UART into buffer; returns number of bytes read.
                 */
                int bytesRead = input.read(buffer);
                // Checks if data was received.
                if (bytesRead > 0) {
                    /**
                     * Converts the bytes to a string.
                     */
                    String data = new String(buffer, 0, bytesRead);
                    /**
                     * Adds the new data to the sentence buffer.
                     */
                    sentence.append(data);
                    int newlineIndex;
                    /**
                     * Loops to find complete lines (ending with \n).
                     */
                    while ((newlineIndex = sentence.indexOf("\n")) != -1) {
                        /**
                         * Extracts a single NMEA sentence.
                         */
                        String line = sentence.substring(0, newlineIndex).trim();
                        /**
                         * Removes the processed line from the buffer.
                         */
                        sentence.delete(0, newlineIndex + 1);
                        /**
                         * Ensures the line isn’t blank.
                         */
                        if (!line.isEmpty()) {
                            /**
                             * Logs the raw NMEA sentence
                             */
                            System.out.println("Raw UART data: " + line);
                            /**
                             * Parses the NMEA sentence and updates latestData
                             */
                            latestData = processNMEA(line);
                        }
                    }
                }
                /**
                 * Pauses 100ms to avoid overwhelming the CPU.
                 */
                Thread.sleep(100);
            }
            /**
             * Handles errors (UART disconnection) and logs them.
             */
        } catch (IOException | InterruptedException e) {
            System.err.println("Error reading UART: " + e.getMessage());
        }
    }
/**
 * 
 * @param data
 * @return 
 */    
    private GPSData processNMEA(String data) {
        /**
         * Splits the NMEA sentence into fields 
         * Example : $GPRMC,230422.00,A,... → ["$GPRMC", "230422.00", "A", ...] .
         */
        String[] parts = data.split(",");
        /**
         * Checks if it’s a $GPRMC sentence with enough fields.
         */
        if (data.startsWith("$GPRMC") && parts.length >= 12) {
            /**
             * Checks if the GPS has a fix (A = active, valid data).
             */
            boolean hasFix = "A".equals(parts[2]);
            if (hasFix && !parts[3].isEmpty() && !parts[4].isEmpty()
                    && !parts[5].isEmpty() && !parts[6].isEmpty()
                    && !parts[7].isEmpty()) {
                try {
                    /**
                     * Converts latitude and direction (like "N") to decimal degrees.
                     */
                    double latitude = parseCoordinate(parts[3], parts[4]);
                    /**
                     * Converts longitude and direction (like "N") to decimal degrees.
                     */
                    double longitude = parseCoordinate(parts[5], parts[6]);
                    /**
                     * Stores direction indicators (e.g., "N", "E").
                     */
                    String latDir = parts[4];
                    String lonDir = parts[6];
                    /**
                     * Converts knots to km/h (1 knot = 1.852 km/h).
                     * - The knot is a unit of speed equal to one nautical mile per hour,
                     */
                    double speedKnots = Double.parseDouble(parts[7]);
                    double speedKmh = speedKnots * 1.852;
                    /**
                     * Creates a new GPSData object with parsed values and isValid = true.
                     */
                    return new GPSData(speedKmh, latitude, longitude, latDir, lonDir, true);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid GPRMC data: " + data);
                }
            }
            /**
             * Returns a "no fix" GPSData if parsing fails or no fix.
             */
            return GPSData.noFix();
        } else {
            System.out.println("Skipping unsupported or incomplete NMEA frame: " + data);
            return latestData;
        }
    }

    

    public void stop() {
        // Stops the readUART() loop.
        running = false;
        // Checks if the UART port is open.*
        if (serialPort != null && serialPort.isOpen()) {
            // Closes the UART connection .
            serialPort.closePort();
            System.out.println("UART closed");
        }
    }
    
    public GPSData getLatestData() {
        return latestData;
    }

 
    private double parseCoordinate(String value, String direction) {
        try {
            /**
             * Sets degree length (2 for lat, 3 for lon; e.g., "2957.59816" → 29 degrees).
             */
            int degreeLength = (direction.equals("N") || direction.equals("S")) ? 2 : 3;
            /**
             * Extracts degrees (e.g., "29")
             */
            double degrees = Double.parseDouble(value.substring(0, degreeLength));
            /**
             * Converts minutes to decimal (e.g., "57.59816" / 60 ≈ 0.959969).
             */
            double minutes = Double.parseDouble(value.substring(degreeLength)) / 60.0;
            /**
             * Combines to decimal degrees (e.g., 29 + 0.959969 = 29.959969).
             */
            double result = degrees + minutes;
            /**
             * Applies negative sign for S/W coordinates
             */
            return (direction.equals("S") || direction.equals("W")) ? -result : result;
            
         /**
           * Handles parsing errors, logs them, and returns 0.0.
           */  
        } catch (Exception e) {
            System.out.println("Invalid coordinate: " + value + " " + direction);
            return 0.0;
        }
    }
}