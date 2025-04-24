/***
 * PrimaryController.java:
 * - Manages the UI (speedometer, labels) and updates it with GPS data in a separate thread.
 * - It also triggers the speed alarm.
 */

package com.mycompany.speedometer;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {

    @FXML private Gauge speedometer;
    @FXML private Label titleLabel;
    @FXML private Label latitudeLabel;
    @FXML private Label longitudeLabel;
    @FXML private Label warningLabel;
    @FXML private ImageView image;
    @FXML private WebView mapView;
    @FXML private BorderPane rootPane; // Changed to BorderPane to match FXML

    private WebEngine webEngine;
    private volatile boolean running = true;
    private volatile boolean wasConnected = true;
    private Alert disconnectAlert;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupGauge();
        setupAppearance();

        webEngine = mapView.getEngine();
        String mapPath = getClass().getResource("/map.html").toExternalForm();
        System.out.println("Loading map from: " + mapPath);
        webEngine.load(mapPath);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("WebView state: " + newState);
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                System.out.println("Map loaded successfully");
                new Thread(this::updateGPSData).start();
            } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("Map loading failed");
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

    private void setupAppearance() {
        // Set background color to gray (#404040)
        rootPane.setStyle("-fx-background-color: #404040;");

        // Set latitude and longitude labels to glow green (#9FFD32) with italic font
        latitudeLabel.setTextFill(Color.rgb(159, 253, 50));
        longitudeLabel.setTextFill(Color.rgb(159, 253, 50));
        Font italicFont = Font.font("System", FontPosture.ITALIC, 12); // Adjust size if needed
        latitudeLabel.setFont(italicFont);
        longitudeLabel.setFont(italicFont);
    }

    private void updateGPSData() {
        while (running) {
            GPSReader reader = GPSData.getReader();
            if (reader != null) {
                GPSData data = reader.getLatestData();
                final boolean currentConnected = reader.isConnected();
                final boolean prevWasConnected = wasConnected;

                System.out.println("Connection state - wasConnected: " + wasConnected + ", currentConnected: " + currentConnected);

                Platform.runLater(() -> {
                    System.out.println("GUI update running");
                    latitudeLabel.setText(String.format("Latitude: %.6f° %s", data.getLatitude(), data.getLatDirection()));
                    longitudeLabel.setText(String.format("Longitude: %.6f° %s", data.getLongitude(), data.getLonDirection()));
                    speedometer.setValue(data.getSpeedKmh());
                    SpeedAlarm.checkSpeed(data.getSpeedKmh());
                    if (data.getSpeedKmh() > SpeedAlarm.SPEED_LIMIT) {
                        warningLabel.setVisible(true);
                        warningLabel.setText("⚠️ Speed Limit Exceeded!");
                        warningLabel.setTextFill(Color.RED);
                    } else {
                        warningLabel.setVisible(false);
                    }

                    if (webEngine != null) {
                        String script = String.format("updateLocation(%f, %f);", data.getLatitude(), data.getLongitude());
                        System.out.println("Executing script: " + script);
                        try {
                            webEngine.executeScript(script);
                        } catch (Exception e) {
                            System.err.println("Script error: " + e.getMessage());
                        }
                    }

                    System.out.println("Evaluating: !currentConnected=" + !currentConnected + ", prevWasConnected=" + prevWasConnected);
                    if (!currentConnected && prevWasConnected) {
                        System.out.println("Condition met: Showing disconnect alert");
                        showDisconnectAlert();
                        System.out.println("Alert shown: " + (disconnectAlert != null && disconnectAlert.isShowing()));
                    } else if (currentConnected && !prevWasConnected) {
                        System.out.println("Condition met: Hiding disconnect alert");
                        hideDisconnectAlert();
                    }
                });

                wasConnected = currentConnected;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void showDisconnectAlert() {
        if (disconnectAlert == null) {
            System.out.println("Creating and showing alert");
            disconnectAlert = new Alert(Alert.AlertType.WARNING);
            disconnectAlert.setTitle("GPS Disconnected");
            disconnectAlert.setHeaderText(null);
            disconnectAlert.setContentText("GPS module disconnected. Attempting to reconnect...");
            disconnectAlert.show();
        }
    }

    private void hideDisconnectAlert() {
        if (disconnectAlert != null) {
            System.out.println("Hiding alert");
            disconnectAlert.hide();
            disconnectAlert = null;
        }
    }

    public void stop() {
        running = false;
        hideDisconnectAlert();
    }
}


