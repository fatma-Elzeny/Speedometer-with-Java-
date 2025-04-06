package com.mycompany.gauge;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class PrimaryController {

    @FXML
    private StackPane gaugePane;

    private Gauge speedometer;

    @FXML
    public void initialize() {
        speedometer = GaugeBuilder.create()
            .skinType(Gauge.SkinType.DASHBOARD)
            .title("Speedometer")
            .unit("km/h")
            .minValue(0)
            .maxValue(240)
            .startAngle(270)
            .angleRange(270)
            .valueVisible(true)
            .majorTickSpace(20)
            .minorTickSpace(5)
            .animated(true)
            .build();

        gaugePane.getChildren().add(speedometer);
    }

    // Add methods to update gauge value if needed
    public void setSpeed(double speed) {
        speedometer.setValue(speed);
    }
}