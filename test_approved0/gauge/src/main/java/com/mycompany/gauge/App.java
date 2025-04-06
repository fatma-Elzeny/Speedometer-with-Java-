package com.mycompany.gauge;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.skins.ModernSkin;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class App extends Application {

    private static Scene scene;
    private Gauge gauge;
    private Label latLabel;
    private Label lonLabel;
    private SerialPort serialPort;
    private volatile boolean running = true;
    private Clip alertSound;
    private boolean speedAlertTriggered = false;
    private double latestSpeedKmh = 0.0;
    private double latestLat = 0.0;
    private double latestLon = 0.0;
    private String latestLatDir = "N/A";
    private String latestLonDir = "N/A";
    private boolean hasFix = false;

    @Override
    public void start(Stage stage) throws IOException {
        Parent fxmlRoot = loadFXML("primary");

        gauge = new Gauge();
        gauge.setSkin(new ModernSkin(gauge));
        gauge.setTitle("ITI SpeedoMeter");
        gauge.setUnit("Km / h");
        gauge.setUnitColor(Color.WHITE);
        gauge.setDecimals(0);
        gauge.setValue(0.0);
        gauge.setAnimated(true);
        gauge.setValueColor(Color.WHITE);
        gauge.setTitleColor(Color.WHITE);
        gauge.setSubTitleColor(Color.WHITE);
        gauge.setBarColor(Color.rgb(0, 214, 215));
        gauge.setNeedleColor(Color.RED);
        gauge.setThresholdColor(Color.RED);
        gauge.setThreshold(85);
        gauge.setThresholdVisible(true);
        gauge.setTickLabelColor(Color.rgb(151, 151, 151));
        gauge.setTickMarkColor(Color.WHITE);
        gauge.setTickLabelOrientation(TickLabelOrientation.ORTHOGONAL);
        gauge.setPrefSize(400, 400);

        latLabel = new Label("Latitude: Waiting for fix...");
        latLabel.setTextFill(Color.WHITE);
        latLabel.setStyle("-fx-font-size: 16px;");

        lonLabel = new Label("Longitude: Waiting for fix...");
        lonLabel.setTextFill(Color.WHITE);
        lonLabel.setStyle("-fx-font-size: 16px;");

        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/alert.wav"));
            alertSound = AudioSystem.getClip();
            alertSound.open(audioIn);
            System.out.println("Alert sound loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load alert sound: " + e.getMessage());
            e.printStackTrace();
        }

        VBox root = new VBox(10, fxmlRoot, gauge, latLabel, lonLabel);
        root.setStyle("-fx-background-color: #333333;");
        scene = new Scene(root, 640, 480);

        stage.setScene(scene);
        stage.setTitle("Speedometer Demo");
        stage.show();

        setupUART();
    }

    private void setupUART() {
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

    private void readUART() {
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
                            processNMEA(line);
                        }
                    }
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error reading UART: " + e.getMessage());
        }
    }

private void processNMEA(String data) {
    String[] parts = data.split(",");

    if (data.startsWith("$GPRMC") && parts.length >= 12) {
        // Example: $GPRMC,152527.00,A,3004.24515,N,03101.26937,E,2.583,,050425,,,A*76
        hasFix = "A".equals(parts[2]);

        if (hasFix && !parts[3].isEmpty() && !parts[4].isEmpty()
                && !parts[5].isEmpty() && !parts[6].isEmpty()
                && !parts[7].isEmpty()) {
            try {
                latestLat = parseCoordinate(parts[3], parts[4]);  // Latitude
                latestLon = parseCoordinate(parts[5], parts[6]);  // Longitude
                latestLatDir = parts[4];
                latestLonDir = parts[6];

                double speedKnots = Double.parseDouble(parts[7]); // Speed in knots
                latestSpeedKmh = speedKnots * 1.852;              // Convert to km/h

            } catch (NumberFormatException e) {
                System.out.println("Invalid GPRMC data: " + data);
            }
        }
    } else {
        System.out.println("Skipping unsupported or incomplete NMEA frame: " + data);
    }

    // Handle speed alert
    if (hasFix) {
        if (latestSpeedKmh > 80 && !speedAlertTriggered) {
            speedAlertTriggered = true;
            if (alertSound != null) {
                alertSound.setFramePosition(0);
                alertSound.start();
                System.out.println("Speed exceeded 80 km/h - Alert triggered!");
            }
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Speed Warning");
                alert.setHeaderText("Speed Limit Exceeded");
                alert.setContentText("Your speed is " + String.format("%.2f", latestSpeedKmh) + " km/h, exceeding 80 km/h!");
                alert.show();
            });
        } else if (latestSpeedKmh <= 80 && speedAlertTriggered) {
            speedAlertTriggered = false;
        }
    }

    // Update UI
    Platform.runLater(() -> {
        gauge.setValue(latestSpeedKmh);
        latLabel.setText(String.format("Latitude: %.6f° %s", Math.abs(latestLat), latestLatDir));
        lonLabel.setText(String.format("Longitude: %.6f° %s", Math.abs(latestLon), latestLonDir));
    });
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


    @Override
    public void stop() {
        running = false;
        if (alertSound != null && alertSound.isRunning()) {
            alertSound.stop();
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("UART closed");
        }
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
