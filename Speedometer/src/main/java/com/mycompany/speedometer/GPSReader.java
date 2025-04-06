package com.mycompany.speedometer;

import com.fazecast.jSerialComm.SerialPort;
import com.mycompany.speedometer.GPSData;
import java.io.IOException;
import java.io.InputStream;

public class GPSReader {
    private SerialPort serialPort;
    private volatile boolean running = true;
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
        //    new Thread(this::readUART).start();
        } else {
            System.err.println("Failed to open UART on /dev/ttyS0");
        }
    }


    

    public void stop() {
        running = false;
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("UART closed");
        }
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