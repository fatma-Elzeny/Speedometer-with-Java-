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

    private volatile boolean running = true;
    private GPSReader reader;

    // Setter to receive GPSReader from App
    public void setGPSReader(GPSReader reader) {
        this.reader = reader;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupGauge();
        updateGPSData();
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
                if (reader != null) { // Ensure reader is set before accessing
                    GPSData data = reader.getLatestData();

                    Platform.runLater(() -> {
                        latitudeLabel.setText(String.format("Latitude: %.6f° %s", data.getLatitude(), data.getLatDirection()));
                        longitudeLabel.setText(String.format("Longitude: %.6f° %s", data.getLongitude(), data.getLonDirection()));
                        speedometer.setValue(data.getSpeedKmh());
                        SpeedAlarm.checkSpeed(data.getSpeedKmh());
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
        // No need to stop reader here; App handles it
    }
}