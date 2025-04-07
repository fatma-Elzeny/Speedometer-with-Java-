/***
 * PrimaryController.java:
 * - Manages the UI (speedometer, labels) and updates it with GPS data in a separate thread.
 * - It also triggers the speed alarm.
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
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

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
    @FXML
    private ImageView image;
    @FXML
    private WebView mapView;
    
    private WebEngine webEngine;

    
    /***
     * running: boolean (volatile) - Controls the update thread.
     */
    private volatile boolean running = true;


    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        setupGauge();
       // updateGPSData();
        
        webEngine = mapView.getEngine();
        String mapPath = getClass().getResource("/map.html").toExternalForm();
        webEngine.load(mapPath);

        // Wait for page to finish loading
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
        if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
            // Only start updating once the map is ready
             updateGPSData();  
        }
    });
    
    }

    private void setupGauge() {
        speedometer.setValue(0);
        speedometer.setMinValue(0);
        speedometer.setMaxValue(160);
        speedometer.setTitle("Speed");
        speedometer.setUnit("KM/H");
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
        new Thread(() -> {
            while (running) {
                GPSReader reader = GPSData.getReader(); // Get the reader from GPSData
                if (reader != null) { // Check if reader is available
                    GPSData data = reader.getLatestData();

                    Platform.runLater(() -> {
                        latitudeLabel.setText(String.format("Latitude: %.6f° %s", data.getLatitude(), data.getLatDirection()));
                        longitudeLabel.setText(String.format("Longitude: %.6f° %s", data.getLongitude(), data.getLonDirection()));
                        speedometer.setValue(data.getSpeedKmh());
                        SpeedAlarm.checkSpeed(data.getSpeedKmh());
                        if(data.getSpeedKmh()> SpeedAlarm.SPEED_LIMIT)
                        {
                            warningLabel.setVisible(true);
                            warningLabel.setText("⚠️ Speed Limit Exceeded!");
                            warningLabel.setTextFill(Color.RED);
                        }
                      if (webEngine != null)
                        {
                            String script = String.format("updateLocation(%f, %f);", data.getLatitude(), data.getLongitude());
                            webEngine.executeScript(script);
                        }
                    });
                }

                try {
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