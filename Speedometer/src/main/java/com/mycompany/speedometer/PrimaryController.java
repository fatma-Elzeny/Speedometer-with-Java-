/**
 * Where: PrimaryController.initialize() and PrimaryController.updateGPSData()
 * What Happens: The UI loads, configures the speedometer, and starts a thread to update the UI with GPS data.
 */

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
/**
 *
 * @author fatma
 */
package com.mycompany.speedometer;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class PrimaryController implements Initializable {

    /**
     * UI components
     */
    
    @FXML
    private Gauge speedometer;
    @FXML
    private Label titleLabel; 
    @FXML
    private Label latitudeLabel;
    @FXML
    private Label longitudeLabel;
    @FXML
    private Label warningLabel;
    /***
     * running: boolean (volatile) - Controls the update thread.
     */
    private volatile boolean running = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /**
         * Configures the speedometer UI (detailed below).
         */
        setupGauge();
        /**
         * Starts a thread to update the UI with GPS data
         */
        updateGPSData();
    }

    private void setupGauge() {
        speedometer.setValue(0);//Sets initial speed to 0 km/h.
        /**
         * Sets speedometer range (0-160 km/h).
         */
        speedometer.setMinValue(0);
        speedometer.setMaxValue(160);
        /**
         * Labels the gauge as "Speed" in "KM/H".
         */
        speedometer.setTitle("Speed");
        speedometer.setUnit("KM/H");
        /**
         * Enables smooth needle animation and auto-scaling.
         */
        speedometer.setAnimated(true);
        speedometer.setAutoScale(true);
        speedometer.setNeedleColor(javafx.scene.paint.Color.RED);
        speedometer.setBackgroundPaint(javafx.scene.paint.Color.BLACK);
        speedometer.setBorderPaint(javafx.scene.paint.Color.DARKGRAY);
        speedometer.setValueColor(javafx.scene.paint.Color.WHITE);
        speedometer.setTitleColor(javafx.scene.paint.Color.LIGHTGRAY);
        speedometer.setUnitColor(javafx.scene.paint.Color.YELLOW);
        speedometer.setTickLabelColor(javafx.scene.paint.Color.LIGHTGRAY);
        speedometer.setSectionsVisible(true);
        speedometer.setSections(
                new Section(0, 50, Color.GREEN),
                new Section(50, 100, Color.YELLOW),
                new Section(100, 160, Color.RED)
        );
        warningLabel.setVisible(false);
    }

    private void updateGPSData() {
        /**
         * Starts a background thread to update the UI every 2 seconds.
         */
        new Thread(() -> {
            /**
             * Loops as long as running is true (stops when stop() is called).
             */
            while (running) {
                /**
                 * Gets the GPSReader instance set by App from GPSData.
                 */
                GPSReader reader = GPSData.getReader();
                /**
                 * Ensures the reader is available before proceeding
                 */
                if (reader != null) { 
                    //Retrieves the latest parsed GPS data from GPSReader.
                    GPSData data = reader.getLatestData();
                    /**
                     * Runs UI updates on the JavaFX Application Thread (required for UI changes).
                     */
                    Platform.runLater(() -> {
                        /**
                         * Updates the latitude, Longitude labels, and the speedometer with the speed on km/h
                         */
                        latitudeLabel.setText(String.format("Latitude: %.6f° %s", data.getLatitude(), data.getLatDirection()));
                        longitudeLabel.setText(String.format("Longitude: %.6f° %s", data.getLongitude(), data.getLonDirection()));
                        speedometer.setValue(data.getSpeedKmh());
                        /**
                         * Checks if the speed triggers the alarm , and appeare label warning message 
                         */
                        SpeedAlarm.checkSpeed(data.getSpeedKmh());
                        if(data.getSpeedKmh()> SpeedAlarm.SPEED_LIMIT)
                        {
                            warningLabel.setVisible(true);
                            warningLabel.setText("⚠️ Speed Limit Exceeded!");
                            warningLabel.setTextFill(Color.RED);
                        }
                    });
                }

                try {
                    /**
                     * Pauses 2 seconds between updates.
                     */
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
    }
}