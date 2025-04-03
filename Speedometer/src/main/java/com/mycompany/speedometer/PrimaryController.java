/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.speedometer;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author fatma
 */
public class PrimaryController implements Initializable {

    @FXML
    private Gauge speedometer;
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView image;
    @FXML
    private Label latitudeLabel;
    @FXML
    private Label longitudeLabel;

    /**
     * Initializes the controller class.
     */
    private final Random random = new Random();
    @FXML
    private Label warningLabel;

    @Override

    public void initialize(URL url, ResourceBundle rb) {

        setupGauge();
        updateSpeedometer();

    }

    private void setupGauge() {

        // Set initial speed
        speedometer.setValue(80);
        speedometer.setMinValue(0);
        speedometer.setMaxValue(160);
        speedometer.setTitle("Speed");
        speedometer.setUnit("KM/H");
        speedometer.setAnimated(true);
        speedometer.setAutoScale(true);

        // Custom Look
        speedometer.setNeedleColor(javafx.scene.paint.Color.RED);
        speedometer.setBackgroundPaint(javafx.scene.paint.Color.BLACK);
        speedometer.setBorderPaint(javafx.scene.paint.Color.DARKGRAY);
        speedometer.setValueColor(javafx.scene.paint.Color.WHITE);
        speedometer.setTitleColor(javafx.scene.paint.Color.LIGHTGRAY);
        speedometer.setUnitColor(javafx.scene.paint.Color.YELLOW);
        speedometer.setTickLabelColor(javafx.scene.paint.Color.LIGHTGRAY);
        speedometer.setSectionsVisible(true);
        speedometer.setSections(
                new Section(0, 50, Color.GREEN), // Safe speed
                new Section(50, 100, Color.YELLOW), // Warning speed
                new Section(100, 160, Color.RED) // Danger speed
        );
        warningLabel.setVisible(false);
    }

    private void updateSpeedometer() {
        new Thread(() -> {
            try {
                while (true) {
                    double speed = random.nextDouble() * 160;
                    double latitude = 30.0 + random.nextDouble();  // Example latitude
                    double longitude = 31.0 + random.nextDouble(); // Example longitude

                    Platform.runLater(() -> {
                        speedometer.setValue(speed);
                        latitudeLabel.setText(String.format("Latitude: %.6f", latitude));
                        longitudeLabel.setText(String.format("Longitude: %.6f", longitude));
                        if (speed > 120) {
                            warningLabel.setText("⚠️ Speed Limit Exceeded!");
                            warningLabel.setTextFill(Color.RED);
                            warningLabel.setVisible(true);
                        } else {
                            warningLabel.setVisible(false);
                        }
                    });

                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
