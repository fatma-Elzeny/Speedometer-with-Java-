package com.mycompany.speedometer;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ResourceBundle;

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
    private GPSReader gpsReader; // Instance field for GPSReader

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupGauge();
        updateGPSData();
    }

    public void setGPSReader(GPSReader reader) {
        this.gpsReader = reader; // Setter for injection
    }

    private void setupGauge() {
        speedometer.setValue(0);
        speedometer.setMinValue(0);
        speedometer.setMaxValue(160);
        speedometer.setTitle("Speed");
        speedometer.setUnit("KM/H");
        speedometer.setAnimated(true);
        speedometer.setAutoScale(true);
        speedometer.setNeedleColor(Color.RED);
        speedometer.setBackgroundPaint(Color.BLACK);
        speedometer.setBorderPaint(Color.DARKGRAY);
        speedometer.setValueColor(Color.WHITE);
        speedometer.setTitleColor(Color.LIGHTGRAY);
        speedometer.setUnitColor(Color.YELLOW);
        speedometer.setTickLabelColor(Color.LIGHTGRAY);
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
                if (gpsReader == null) { // Safety check until injected
                    try {
                        Thread.sleep(2000);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                double speed = gpsReader.getSpeed();
                double latitude = gpsReader.getLatitude();
                double longitude = gpsReader.getLongitude();
                String latitudeDir = gpsReader.getLatitudeDir();
                String longitudeDir = gpsReader.getLongitudeDir();
                boolean hasFix = gpsReader.hasFix();

                Platform.runLater(() -> {
                    latitudeLabel.setText(String.format("Latitude: %.6f° %s", Math.abs(latitude), latitudeDir));
                    longitudeLabel.setText(String.format("Longitude: %.6f° %s", Math.abs(longitude), longitudeDir));
                    speedometer.setValue(speed);
                    SpeedAlarm.checkSpeed(speed);
                    if (hasFix && speed > 120) {
                        warningLabel.setText("⚠️ Speed Limit Exceeded!");
                        warningLabel.setTextFill(Color.RED);
                        warningLabel.setVisible(true);
                    } else {
                        warningLabel.setVisible(false);
                    }
                });

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