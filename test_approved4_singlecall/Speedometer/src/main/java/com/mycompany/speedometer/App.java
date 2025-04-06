package com.mycompany.speedometer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        GPSReader reader = new GPSReader(); // Create the single GPSReader
        reader.setupUART(); // Start listening to the GPS (only here)

        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Parent root = loader.load();
        PrimaryController controller = loader.getController(); // Get the controller
        controller.setGPSReader(reader); // Pass the GPSReader to the controller
        root.setUserData(controller); // Store the controller in userData

        scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.setTitle("Speedometer Demo");
        stage.show();
    }

    @Override
    public void stop() {
        Object userData = scene.getRoot().getUserData();
        if (userData instanceof PrimaryController) {
            GPSReader reader = ((PrimaryController) userData).getGPSReader();
            if (reader != null) {
                reader.stop(); // Stop the GPSReader
            }
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