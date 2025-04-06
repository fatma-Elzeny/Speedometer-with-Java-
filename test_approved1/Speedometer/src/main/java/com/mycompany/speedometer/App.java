package com.mycompany.speedometer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private GPSReader gpsReader;

    @Override
    public void start(Stage stage) throws IOException {
        gpsReader = new GPSReader();
        gpsReader.setupUART();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Parent root = loader.load();
        PrimaryController controller = loader.getController();
        controller.setGPSReader(gpsReader); // Inject GPSReader
        scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.setTitle("Speedometer Demo");
        stage.show();
    }

    @Override
    public void stop() {
        if (gpsReader != null) {
            gpsReader.stopReading();
        }
        SpeedAlarm.shutdown();
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