package com.mycompany.speedometer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private GPSReader reader;

    @Override
    public void start(Stage stage) throws IOException {
        reader = new GPSReader();
        reader.setupUART(); // Start UART reading once

        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Parent root = loader.load();
        PrimaryController controller = loader.getController();
        controller.setGPSReader(reader); // Pass GPSReader to controller

        scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.setTitle("Speedometer Demo");
        stage.show();
    }

    @Override
    public void stop() {
        if (reader != null) {
            reader.stop();
        }
        SpeedAlarm.stop();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}